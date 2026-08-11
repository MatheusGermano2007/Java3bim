package controller;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import model.dto.LinguagemDTO;
import service.LinguagemService;
import util.DialogUtil;
import util.LinguagemValidador;

import java.net.URL;
import java.util.ResourceBundle;

// essa classe eh a dona da tela toda e controla o que acontece
public class MainController implements Initializable {

    // variaveis da tela que o javafx liga pra gente sozinho
    @FXML private Label lblTotal, lblMensagem;
    @FXML private TableView<LinguagemDTO> tableView;
    @FXML private TableColumn<LinguagemDTO, String> colNome, colCriador, colTipo, colAno;
    @FXML private TextField txtSearch, txtNome, txtCriador, txtAno;
    @FXML private ComboBox<String> comboTipo;
    @FXML private Button btnSave, btnClear, btnUpdate, btnDelete;

    // chama o servico que faz o trabalho pesado com o banco de dados
    private final LinguagemService service = new LinguagemService();
    // listinha salva em memoria pra busca ficar bem rapida
    private final ObservableList<LinguagemDTO> masterData = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // isso roda logo que a tela abre

        // liga as colunas da tabela com as variaveis certas
        colNome.setCellValueFactory(new PropertyValueFactory<>("nome"));
        colCriador.setCellValueFactory(new PropertyValueFactory<>("criador"));
        colTipo.setCellValueFactory(new PropertyValueFactory<>("tipo"));
        colAno.setCellValueFactory(new PropertyValueFactory<>("anoCriacao"));

        // bota as opcoes no select de tipo
        comboTipo.getItems().addAll("Orientado a Objetos", "Funcional", "Backend", "Frontend");
        // texto padrao pra quando a tabela tiver vazia
        tableView.setPlaceholder(new Label("Nenhuma linguagem encontrada"));

        // prepara os cliques da tela e busca os dados
        configurarEventos();
        recarregarTabela();
        // ja trava os botoes no inicio
        limparCampos();
        // joga o cursor de digitar pro primeiro campo sozinho
        Platform.runLater(txtNome::requestFocus);
    }

    private void configurarEventos() {
        // se clicar em alguem na tabela ja preenche os campos pra editar
        tableView.getSelectionModel().selectedItemProperty().addListener((obs, old, selecionada) -> {
            boolean temSelecao = selecionada != null;
            btnUpdate.setDisable(!temSelecao);
            btnDelete.setDisable(!temSelecao);

            if (temSelecao) {
                txtNome.setText(selecionada.getNome());
                txtCriador.setText(selecionada.getCriador());
                comboTipo.setValue(selecionada.getTipo());
                txtAno.setText(String.valueOf(selecionada.getAnoCriacao()));
            }
        });

        // toda vez que digitar algo ele checa se da pra liberar os botoes
        javafx.beans.value.ChangeListener<String> validador = (obs, oldV, newV) -> validarBotoes();
        txtNome.textProperty().addListener(validador);
        txtCriador.textProperty().addListener(validador);
        txtAno.textProperty().addListener(validador);
    }

    // trava o botao de salvar se faltar preencher coisa
    private void validarBotoes() {
        btnSave.setDisable(LinguagemValidador.algumCampoVazio(txtNome.getText(), txtCriador.getText(), txtAno.getText()));
        btnClear.setDisable(LinguagemValidador.todosCamposVazios(txtNome.getText(), txtCriador.getText(), txtAno.getText()));
    }

    // tenta salvar uma linguagem nova no banco
    @FXML
    private void acaoSalvar() {
        Integer ano = LinguagemValidador.converterAno(txtAno.getText());
        if (ano == null) { DialogUtil.showError("O ano deve ser numérico"); return; }

        try {
            service.cadastrarLinguagem(new LinguagemDTO(txtNome.getText(), txtCriador.getText(), comboTipo.getValue(), ano));
            finalizarAcao("Salvo com sucesso!");
        } catch (Exception e) { DialogUtil.showError("Erro: " + e.getMessage()); }
    }

    // pega o que ta na tela e atualiza a linguagem que foi clicada
    @FXML
    private void acaoAtualizar() {
        LinguagemDTO dto = tableView.getSelectionModel().getSelectedItem();
        Integer ano = LinguagemValidador.converterAno(txtAno.getText());

        if (dto == null || ano == null) { mostrarMensagem("Selecione um item e use um ano válido", false); return; }
        if (LinguagemValidador.semAlteracoes(dto, txtNome.getText(), txtCriador.getText(), comboTipo.getValue(), ano)) {
            mostrarMensagem("Nenhuma alteração feita", false); return;
        }

        try {
            dto.setNome(txtNome.getText()); dto.setCriador(txtCriador.getText());
            dto.setTipo(comboTipo.getValue()); dto.setAnoCriacao(ano);
            service.atualizarLinguagem(dto);
            finalizarAcao("Atualizado com sucesso!");
        } catch (Exception e) { mostrarMensagem("Erro: " + e.getMessage(), false); }
    }

    // pergunta se quer apagar real e manda o service deletar
    @FXML
    private void acaoExcluir() {
        LinguagemDTO dto = tableView.getSelectionModel().getSelectedItem();
        if (dto == null) return;

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Apagar " + dto.getNome() + " permanentemente?");
        if (alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            try {
                service.excluirLinguagem(dto.getId());
                finalizarAcao("Excluído com sucesso!");
            } catch (Exception e) { mostrarMensagem("Erro: " + e.getMessage(), false); }
        }
    }

    // apaga tudo dos campos de texto e desmarca a tabela pra recomecar
    @FXML
    public void limparCampos() {
        txtNome.clear(); txtCriador.clear(); txtAno.clear();
        if (comboTipo != null) comboTipo.getSelectionModel().clearSelection();

        tableView.getSelectionModel().clearSelection();
        validarBotoes();
        txtNome.requestFocus();

        mostrarMensagem("Sistema pronto.", true);
        lblMensagem.setStyle("-fx-text-fill: #888;");
    }

    // atalho maroto pra nao ficar repetindo codigo toda hora
    private void finalizarAcao(String msg) {
        recarregarTabela();
        limparCampos();
        mostrarMensagem(msg, true);
        if (msg.contains("Salvo")) DialogUtil.showInfo(msg);
    }

    // busca do banco de novo e faz a barrinha de pesquisa funcionar
    private void recarregarTabela() {
        try {
            masterData.setAll(service.listarLinguagens());
            FilteredList<LinguagemDTO> filtrada = new FilteredList<>(masterData, p -> true);

            if (txtSearch != null) {
                txtSearch.textProperty().addListener((obs, old, novo) -> {
                    filtrada.setPredicate(lang -> novo == null || novo.isEmpty() ||
                            lang.getNome().toLowerCase().contains(novo.toLowerCase()) ||
                            lang.getCriador().toLowerCase().contains(novo.toLowerCase()));
                    if (lblTotal != null) lblTotal.setText("Total: " + filtrada.size());
                });
            }
            tableView.setItems(filtrada);
            if (lblTotal != null) lblTotal.setText("Total: " + filtrada.size());

        } catch (Exception e) { e.printStackTrace(); }
    }

    // pinta o textinho de verde ou vermelho dependendo se deu bom ou ruim
    private void mostrarMensagem(String txt, boolean sucesso) {
        if (lblMensagem != null) {
            lblMensagem.setText(txt);
            lblMensagem.setStyle("-fx-font-weight: bold; -fx-text-fill: " + (sucesso ? "#00ff00;" : "#ff4c4c;"));
        }
    }
}