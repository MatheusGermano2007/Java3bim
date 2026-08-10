package controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import model.dto.LinguagemDTO;
import service.LinguagemService;

import java.net.URL;
import java.util.ResourceBundle;

public class MainController implements Initializable {

    @FXML private Label lblTotal, lblMensagem;
    @FXML private TableView<LinguagemDTO> tableView;
    @FXML private TableColumn<LinguagemDTO, String> colNome, colCriador, colTipo, colAno;
    @FXML private TextField txtSearch, txtNome, txtCriador, txtAno;
    @FXML private ComboBox<String> comboTipo;
    @FXML private Button btnSave, btnClear, btnUpdate, btnDelete;

    // Instancia o serviço normal
    private final LinguagemService service = new LinguagemService();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Passa todos os componentes da tela pro Service configurar e controlar
        service.iniciarTela(
                tableView, colNome, colCriador, colTipo, colAno,
                txtSearch, txtNome, txtCriador, txtAno, comboTipo,
                btnSave, btnClear, btnUpdate, btnDelete, lblTotal, lblMensagem
        );
    }

    // O Controller ficou só de enfeite, o serviço faz td

    @FXML
    private void acaoSalvar() {
        service.salvar();
    }

    @FXML
    private void acaoAtualizar() {
        service.atualizar();
    }

    @FXML
    private void acaoExcluir() {
        service.excluir();
    }

    @FXML
    public void limparCampos() {
        service.limparCampos();
    }
}