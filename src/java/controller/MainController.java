package controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import model.dto.LinguagemDTO;
import service.LinguagemService;
import java.net.URL;
import java.util.ResourceBundle;
import util.validation.LinguagemValidador;

public class MainController implements Initializable {

    @FXML private Label lblTotal, lblMensagem;
    @FXML private TableView<LinguagemDTO> tableView;
    @FXML private TableColumn<LinguagemDTO, String> colNome, colCriador, colTipo, colAno;
    @FXML private TextField txtSearch, txtNome, txtCriador, txtAno;
    @FXML private ComboBox<String> comboTipo;
    @FXML private Button btnSave, btnClear, btnUpdate, btnDelete;

    private final LinguagemService service = new LinguagemService();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        service.inicializarTabela(tableView, colNome, colCriador, colTipo, colAno, comboTipo);
        service.configurarEventos(tableView, txtNome, txtCriador, comboTipo, txtAno, btnSave, btnClear, btnUpdate, btnDelete, txtSearch, lblTotal);
        service.recarregarTabela(tableView, txtSearch, lblTotal);
        limparCampos();
        Platform.runLater(txtNome::requestFocus);
    }

    @FXML
    private void acaoSalvar() {
        // 1. O Controller chama o Validador
        LinguagemValidador validador = new LinguagemValidador();
        boolean dadosValidos = validador.validarCadastro(txtNome.getText(), txtCriador.getText(), txtAno.getText());

        // 2. Só chama o Service para salvar se os dados estiverem válidos
        if (dadosValidos) {
            if (service.acaoSalvar(txtNome, txtCriador, comboTipo, txtAno, lblMensagem)) {
                posAcao();
            }
        }
    }

    @FXML
    private void acaoAtualizar() {
        if (service.acaoAtualizar(tableView, txtNome, txtCriador, comboTipo, txtAno, lblMensagem)) {
            posAcao();
        }
    }

    @FXML
    private void acaoExcluir() {
        if (service.acaoExcluir(tableView, lblMensagem)) {
            posAcao();
        }
    }

    @FXML
    public void limparCampos() {
        service.limparCamposVisuais(txtNome, txtCriador, txtAno, comboTipo, tableView, btnSave, btnClear, lblMensagem);
    }

    private void posAcao() {
        service.recarregarTabela(tableView, txtSearch, lblTotal);
        limparCampos();
    }
}