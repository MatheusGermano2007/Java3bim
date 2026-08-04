package controller;

import javafx.scene.control.cell.PropertyValueFactory;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import model.dao.LinguagemDAO;
import model.dto.LinguagemDTO;
import java.util.List;
import java.net.URL;
import java.util.ResourceBundle;
import util.DialogUtil;
import util.LinguagemValidador;

// Controller da tela principal: conecta o FXML à lógica e ao banco de dados
public class MainController implements Initializable {

    // Componentes do FXML exatos como estão no seu layout
    @FXML private Label lblTotal;
    @FXML private TableView<LinguagemDTO> tableView;
    @FXML private TableColumn<LinguagemDTO, String> colNome;
    @FXML private TableColumn<LinguagemDTO, String> colCriador;
    @FXML private TableColumn<LinguagemDTO, String> colTipo;
    @FXML private TableColumn<LinguagemDTO, String> colAno;

    @FXML private TextField txtSearch;
    @FXML private TextField txtNome;
    @FXML private TextField txtCriador;
    @FXML private ComboBox<String> comboTipo;
    @FXML private TextField txtAno;

    @FXML private Button btnSave;
    @FXML private Button btnClear;
    @FXML private Button btnUpdate;
    @FXML private Button btnDelete;

    @FXML private Label lblMensagem;

    // Configuração inicial da tela, chamada automaticamente ao carregar o FXML
    @Override
    public void initialize(URL location, ResourceBundle resources) {


        // 1. Textos de dica invisíveis que guiam o usuário
        if (txtSearch != null) txtSearch.setPromptText("🔍 Pesquisar por nome ou criador...");
        if (txtNome != null) txtNome.setPromptText("Ex: Java");
        if (txtCriador != null) txtCriador.setPromptText("Ex: James Gosling");
        if (txtAno != null) txtAno.setPromptText("Ex: 1995");
        if (comboTipo != null) comboTipo.setPromptText("Selecione um tipo...");

        // 2. Estado Vazio para a Tabela
        Label avisoTabelaVazia = new Label("Nenhuma linguagem encontrada.");

        avisoTabelaVazia.setStyle("-fx-text-fill: #a0a0a0; -fx-font-size: 14px; -fx-font-style: italic;");
        tableView.setPlaceholder(avisoTabelaVazia);

        // Liga as colunas da tabela aos atributos do DTO
        colNome.setCellValueFactory(new PropertyValueFactory<>("nome"));
        colCriador.setCellValueFactory(new PropertyValueFactory<>("criador"));
        colTipo.setCellValueFactory(new PropertyValueFactory<>("tipo"));
        colAno.setCellValueFactory(new PropertyValueFactory<>("anoCriacao"));

        recarregarTabela(); // Carrega os dados do banco

        // Ao selecionar uma linha, preenche o formulário e libera Atualizar/Excluir
        tableView.getSelectionModel().selectedItemProperty().addListener((obs, selecaoAntiga, novaSelecao) -> {
            boolean temSelecao = (novaSelecao != null);
            btnUpdate.setDisable(!temSelecao);
            btnDelete.setDisable(!temSelecao);

            if (temSelecao) {
                txtNome.setText(novaSelecao.getNome());
                txtCriador.setText(novaSelecao.getCriador());
                comboTipo.setValue(novaSelecao.getTipo());
                txtAno.setText(String.valueOf(novaSelecao.getAnoCriacao()));
            }
        });

        comboTipo.getItems().addAll("Orientado a Objetos", "Funcional", "Backend", "Frontend");

        if (btnSave != null) btnSave.setDisable(true);
        if (btnClear != null) btnClear.setDisable(true);

        // Validação em tempo real dos campos, via LinguagemValidador
        javafx.beans.value.ChangeListener<String> validador = (obs, antigo, novo) -> {
            boolean algumCampoVazio = LinguagemValidador.algumCampoVazio(
                    txtNome.getText(), txtCriador.getText(), txtAno.getText());

            boolean tudoVazio = LinguagemValidador.todosCamposVazios(
                    txtNome.getText(), txtCriador.getText(), txtAno.getText());

            if (btnSave != null) btnSave.setDisable(algumCampoVazio);
            if (btnClear != null) btnClear.setDisable(tudoVazio);
        };

        // Adiciona a validação aos campos de texto
        txtNome.textProperty().addListener(validador);
        txtCriador.textProperty().addListener(validador);
        txtAno.textProperty().addListener(validador);

        // 5. Configura a barra de pesquisa (Filtro em tempo real)
        if (txtSearch != null) {
            txtSearch.textProperty().addListener((obs, antigo, novo) -> {
                recarregarTabela();
            });
        }
        javafx.application.Platform.runLater(() -> {
            if (txtNome != null) {
                txtNome.requestFocus();
            }
        });
    }
    // UX 3: Método para exibir mensagens dinâmicas de feedback na barra inferior
    private void mostrarMensagem(String texto, boolean isSucesso) {
        lblMensagem.setText(texto);
        lblMensagem.getStyleClass().removeAll("msg-sucesso", "msg-erro");
        if (isSucesso) {
            lblMensagem.getStyleClass().add("msg-sucesso");
            lblMensagem.setStyle("-fx-text-fill: #00ff00; -fx-font-weight: bold;"); // Força a cor verde
        } else {
            lblMensagem.getStyleClass().add("msg-erro");
            lblMensagem.setStyle("-fx-text-fill: #ff4c4c; -fx-font-weight: bold;"); // Força a cor vermelha
        }
    }

    // Cadastra uma nova linguagem
    @FXML
    private void acaoSalvar() {
        LinguagemDTO dto = new LinguagemDTO();
        dto.setNome(txtNome.getText());
        dto.setCriador(txtCriador.getText());
        dto.setTipo(comboTipo.getValue());

        Integer ano = LinguagemValidador.converterAno(txtAno.getText());
        if (ano == null) {
            DialogUtil.showError("O ano deve conter apenas números!");
            return;
        }
        dto.setAnoCriacao(ano);

        try {
            LinguagemDAO dao = new LinguagemDAO();
            dao.cadastrarLinguagem(dto);

            recarregarTabela();
            limparCampos();

            // Usa o modal de informação para avisar do sucesso
            DialogUtil.showInfo("Linguagem salva com sucesso!");
        } catch (Exception e) {
            DialogUtil.showError("Erro ao salvar no banco de dados: " + e.getMessage());
        }
    }

    // Exclui a linguagem selecionada, com confirmação
    @FXML
    private void acaoExcluir() {
        LinguagemDTO selecionada = tableView.getSelectionModel().getSelectedItem();

        if (selecionada != null) {
            // 1. Cria a caixinha de confirmação
            javafx.scene.control.Alert alerta = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.CONFIRMATION);
            alerta.setTitle("Confirmação de Exclusão");
            alerta.setHeaderText("Você está prestes a excluir: " + selecionada.getNome());
            alerta.setContentText("Tem certeza que deseja apagar esta linguagem permanentemente?");

            // 2. Mostra a caixinha e verifica qual botão foi clicado
            java.util.Optional<javafx.scene.control.ButtonType> resultado = alerta.showAndWait();

            if (resultado.isPresent() && resultado.get() == javafx.scene.control.ButtonType.OK) {
                // Se clicou em OK, executa a exclusão
                try {
                    LinguagemDAO dao = new LinguagemDAO();
                    // AJUSTE AQUI: Use o nome do seu método de excluir no DAO (ex: deletar, excluir)
                    dao.excluirLinguagem(selecionada.getId());

                    recarregarTabela();
                    limparCampos();
                    mostrarMensagem("Linguagem excluída!", true);
                } catch (Exception e) {
                    mostrarMensagem("Erro ao excluir: " + e.getMessage(), false);
                }
            }
        }
    }

    // Atualiza a linguagem selecionada, se houver mudanças reais
    @FXML
    public void acaoAtualizar() {
        LinguagemDTO selecionada = tableView.getSelectionModel().getSelectedItem();

        if (selecionada != null) {

            // 1. Pega os textos atuais que estão na tela
            String nomeTela = txtNome.getText().trim();
            String criadorTela = txtCriador.getText().trim();
            String tipoTela = comboTipo.getValue();

            Integer anoTela = LinguagemValidador.converterAno(txtAno.getText());
            if (anoTela == null) {
                mostrarMensagem("Erro: O ano deve ser apenas números!", false);
                return;
            }

            // 2. Verifica se algo realmente mudou antes de ir ao banco
            boolean semMudancas = LinguagemValidador.semAlteracoes(
                    selecionada, nomeTela, criadorTela, tipoTela, anoTela);

            // Se nada mudou, avisa o usuário e para por aqui (não vai pro banco de dados)
            if (semMudancas) {
                mostrarMensagem("Nenhuma alteração foi feita.", false);
                return;
            }

            // 3. Se passou pela verificação acima, é porque algo mudou! Atualiza o DTO:
            selecionada.setNome(nomeTela);
            selecionada.setCriador(criadorTela);
            selecionada.setTipo(tipoTela);
            selecionada.setAnoCriacao(anoTela);

            // 4. Manda pro banco de dados
            try {
                LinguagemDAO dao = new LinguagemDAO();
                dao.atualizarLinguagem(selecionada);

                recarregarTabela();
                limparCampos();
                tableView.refresh();

                mostrarMensagem("Linguagem atualizada com sucesso!", true);

                // Desabilita os botões de novo e tira a seleção da tabela
                tableView.getSelectionModel().clearSelection();
                btnUpdate.setDisable(true);
                btnDelete.setDisable(true);

            } catch (Exception e) {
                mostrarMensagem("Erro ao atualizar: " + e.getMessage(), false);
            }
        } else {
            mostrarMensagem("Selecione uma linguagem para atualizar.", false);
        }
    }

    // Reseta o formulário e a seleção da tabela
    @FXML
    public void limparCampos() {
        txtNome.clear();
        txtCriador.clear();
        comboTipo.getSelectionModel().clearSelection();
        txtAno.clear();

        // Remove a marcação da tabela e trava os botões novamente!
        tableView.getSelectionModel().clearSelection();
        if (btnUpdate != null) btnUpdate.setDisable(true);
        if (btnDelete != null) btnDelete.setDisable(true);

        txtNome.requestFocus();
        mostrarMensagem("Campos limpos. Sistema pronto.", true);
        lblMensagem.setStyle("-fx-text-fill: #888;");
    }

    // Busca todos os registros e filtra pela barra de pesquisa
    private void recarregarTabela() {
        try {
            LinguagemDAO dao = new LinguagemDAO();
            List<LinguagemDTO> listaBanco = dao.listarLinguagens();

            // filtro
            String busca = txtSearch.getText().toLowerCase(); // Pega o texto e deixa minúsculo
            List<LinguagemDTO> listaFiltrada = new java.util.ArrayList<>();

            for (LinguagemDTO lang : listaBanco) {
                // Se o nome ou criador contiverem a letra digitada, adiciona na lista
                if (lang.getNome().toLowerCase().contains(busca) ||
                        lang.getCriador().toLowerCase().contains(busca)) {
                    listaFiltrada.add(lang);
                }
            }

            tableView.getItems().setAll(listaFiltrada); // Joga só os filtrados na tabela

            if (lblTotal != null) {
                lblTotal.setText("Total cadastrado: " + listaFiltrada.size());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}