package controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import model.dto.LinguagemDTO;
import service.ILinguagemService;
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

    //  O Controller agora depende da INTERFACE, não da classe concreta
    private final ILinguagemService service;

    //  Construtor padrão obrigatório para o FXMLLoader do JavaFX instanciar a tela
    public MainController() {
        this(new LinguagemService());
    }

    //  Construtor com Injeção de Dependência, usado para testes de unidade com Mocks
    public MainController(ILinguagemService service) {
        this.service = service;
    }

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
        if (service.acaoSalvar(txtNome, txtCriador, comboTipo, txtAno, lblMensagem)) {
            posAcao();
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