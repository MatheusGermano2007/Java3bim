package service;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import model.dao.LinguagemDAO;
import model.dto.LinguagemDTO;
import util.DialogUtil;
import util.LinguagemValidador;

import java.util.List;
import java.util.Optional;

public class LinguagemService {

    // Banco de dados
    private final LinguagemDAO dao;
    private final ObservableList<LinguagemDTO> masterData = FXCollections.observableArrayList();

    // Componentes da Tela
    private TableView<LinguagemDTO> tableView;
    private TextField txtSearch, txtNome, txtCriador, txtAno;
    private ComboBox<String> comboTipo;
    private Button btnSave, btnClear, btnUpdate, btnDelete;
    private Label lblTotal, lblMensagem;

    public LinguagemService() {
        this.dao = new LinguagemDAO();
    }

    // Recebe os componentes do Controller e configura a tela
    public void iniciarTela(TableView<LinguagemDTO> tableView, TableColumn<LinguagemDTO, String> colNome,
                            TableColumn<LinguagemDTO, String> colCriador, TableColumn<LinguagemDTO, String> colTipo,
                            TableColumn<LinguagemDTO, String> colAno, TextField txtSearch, TextField txtNome,
                            TextField txtCriador, TextField txtAno, ComboBox<String> comboTipo,
                            Button btnSave, Button btnClear, Button btnUpdate, Button btnDelete,
                            Label lblTotal, Label lblMensagem) {

        this.tableView = tableView; this.txtSearch = txtSearch; this.txtNome = txtNome;
        this.txtCriador = txtCriador; this.txtAno = txtAno; this.comboTipo = comboTipo;
        this.btnSave = btnSave; this.btnClear = btnClear; this.btnUpdate = btnUpdate;
        this.btnDelete = btnDelete; this.lblTotal = lblTotal; this.lblMensagem = lblMensagem;

        // Configura as colunas
        colNome.setCellValueFactory(new PropertyValueFactory<>("nome"));
        colCriador.setCellValueFactory(new PropertyValueFactory<>("criador"));
        colTipo.setCellValueFactory(new PropertyValueFactory<>("tipo"));
        colAno.setCellValueFactory(new PropertyValueFactory<>("anoCriacao"));


        if (txtSearch != null) txtSearch.setPromptText("🔍 Pesquisar...");
        txtNome.setPromptText("Ex: Java");
        txtCriador.setPromptText("Ex: James Gosling");
        txtAno.setPromptText("Ex: 1995");
        comboTipo.setPromptText("Selecione...");
        comboTipo.getItems().addAll("Orientado a Objetos", "Funcional", "Backend", "Frontend");

        Label aviso = new Label("Nenhuma linguagem encontrada");
        aviso.setStyle("-fx-text-fill: #a0a0a0; -fx-font-style: italic;");
        tableView.setPlaceholder(aviso);

        btnSave.setDisable(true);
        btnClear.setDisable(true);

        configurarEventos();
        recarregarTabela();
        Platform.runLater(txtNome::requestFocus);
    }

    private void configurarEventos() {
        tableView.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newV) -> {
            boolean selecionado = newV != null;
            btnUpdate.setDisable(!selecionado);
            btnDelete.setDisable(!selecionado);

            if (selecionado) {
                txtNome.setText(newV.getNome());
                txtCriador.setText(newV.getCriador());
                comboTipo.setValue(newV.getTipo());
                txtAno.setText(String.valueOf(newV.getAnoCriacao()));
            }
        });

        javafx.beans.value.ChangeListener<String> validador = (obs, oldV, newV) -> {
            btnSave.setDisable(LinguagemValidador.algumCampoVazio(txtNome.getText(), txtCriador.getText(), txtAno.getText()));
            btnClear.setDisable(LinguagemValidador.todosCamposVazios(txtNome.getText(), txtCriador.getText(), txtAno.getText()));
        };

        txtNome.textProperty().addListener(validador);
        txtCriador.textProperty().addListener(validador);
        txtAno.textProperty().addListener(validador);
    }

    public void salvar() {
        Integer ano = LinguagemValidador.converterAno(txtAno.getText());
        if (ano == null) {
            DialogUtil.showError("O ano deve conter apenas números");
            return;
        }

        try {
            LinguagemDTO dto = new LinguagemDTO(txtNome.getText(), txtCriador.getText(), comboTipo.getValue(), ano);
            dao.cadastrarLinguagem(dto); // Vai pro banco!
            recarregarTabela();
            limparCampos();
            DialogUtil.showInfo("Salvo com sucesso");
        } catch (Exception e) {
            DialogUtil.showError("Erro: " + e.getMessage());
        }
    }

    public void atualizar() {
        LinguagemDTO selecionada = tableView.getSelectionModel().getSelectedItem();
        if (selecionada == null) return;

        Integer ano = LinguagemValidador.converterAno(txtAno.getText());
        if (ano == null) { mostrarMensagem("O ano deve ser número", false); return; }

        if (LinguagemValidador.semAlteracoes(selecionada, txtNome.getText(), txtCriador.getText(), comboTipo.getValue(), ano)) {
            mostrarMensagem("Nenhuma alteração feita", false); return;
        }

        try {
            selecionada.setNome(txtNome.getText());
            selecionada.setCriador(txtCriador.getText());
            selecionada.setTipo(comboTipo.getValue());
            selecionada.setAnoCriacao(ano);

            dao.atualizarLinguagem(selecionada); // Vai pro banco!
            recarregarTabela();
            limparCampos();
            mostrarMensagem("Atualizado", true);
        } catch (Exception e) {
            mostrarMensagem("Erro: " + e.getMessage(), false);
        }
    }

    public void excluir() {
        LinguagemDTO selecionada = tableView.getSelectionModel().getSelectedItem();
        if (selecionada == null) return;

        Alert alerta = new Alert(Alert.AlertType.CONFIRMATION, "Tem certeza que deseja apagar permanentemente?");
        alerta.setHeaderText("Apagar: " + selecionada.getNome());

        Optional<ButtonType> resultado = alerta.showAndWait();
        if (resultado.isPresent() && resultado.get() == ButtonType.OK) {
            try {
                dao.excluirLinguagem(selecionada.getId()); // Vai pro banco
                recarregarTabela();
                limparCampos();
                mostrarMensagem("Excluído com sucesso!", true);
            } catch (Exception e) {
                mostrarMensagem("Erro: " + e.getMessage(), false);
            }
        }
    }

    public void limparCampos() {
        txtNome.clear();
        txtCriador.clear();
        if(comboTipo != null) comboTipo.getSelectionModel().clearSelection();
        txtAno.clear();

        tableView.getSelectionModel().clearSelection();
        btnUpdate.setDisable(true);
        btnDelete.setDisable(true);
        txtNome.requestFocus();

        mostrarMensagem("Campos limpos. Pronto.", true);
        lblMensagem.setStyle("-fx-text-fill: #888;");
    }

    private void recarregarTabela() {
        try {
            List<LinguagemDTO> listaBanco = dao.listarLinguagens();
            masterData.setAll(listaBanco);

            FilteredList<LinguagemDTO> listaFiltrada = new FilteredList<>(masterData, p -> true);
            if (txtSearch != null) {
                txtSearch.textProperty().addListener((obs, oldV, newV) -> {
                    listaFiltrada.setPredicate(lang -> {
                        if (newV == null || newV.isEmpty()) return true;
                        String busca = newV.toLowerCase();
                        return lang.getNome().toLowerCase().contains(busca) || lang.getCriador().toLowerCase().contains(busca);
                    });
                    if (lblTotal != null) lblTotal.setText("Total: " + listaFiltrada.size());
                });
            }
            tableView.setItems(listaFiltrada);
            if (lblTotal != null) lblTotal.setText("Total: " + listaFiltrada.size());

        } catch (Exception e) { e.printStackTrace(); }
    }

    private void mostrarMensagem(String texto, boolean isSucesso) {
        if (lblMensagem == null) return;
        lblMensagem.setText(texto);
        lblMensagem.setStyle("-fx-font-weight: bold; -fx-text-fill: " + (isSucesso ? "#00ff00;" : "#ff4c4c;"));
    }
}