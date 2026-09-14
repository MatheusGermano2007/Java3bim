package service;

import javafx.scene.control.*;
import model.dto.LinguagemDTO;

// Contrato que define TUDO que a regra de negócio precisa entregar para a tela
public interface ILinguagemService {

    // Configura as colunas da tabela e os itens da caixa de seleção (ComboBox)
    void inicializarTabela(TableView<LinguagemDTO> tableView, TableColumn<LinguagemDTO, String> colNome, TableColumn<LinguagemDTO, String> colCriador, TableColumn<LinguagemDTO, String> colTipo, TableColumn<LinguagemDTO, String> colAno, ComboBox<String> comboTipo);

    // Ouve os cliques da tabela, atava/desativa botões e escuta a digitação
    void configurarEventos(TableView<LinguagemDTO> tableView, TextField txtNome, TextField txtCriador, ComboBox<String> comboTipo, TextField txtAno, Button btnSave, Button btnClear, Button btnUpdate, Button btnDelete, TextField txtSearch, Label lblTotal);

    // Busca as linguagens no banco e atualiza a tabela (com suporte a busca)
    void recarregarTabela(TableView<LinguagemDTO> tableView, TextField txtSearch, Label lblTotal);

    // Valida os dados da tela e grava uma nova linguagem no banco
    boolean acaoSalvar(TextField txtNome, TextField txtCriador, ComboBox<String> comboTipo, TextField txtAno, Label lblMensagem);

    // Valida e atualiza os dados do item selecionado na tabela
    boolean acaoAtualizar(TableView<LinguagemDTO> tableView, TextField txtNome, TextField txtCriador, ComboBox<String> comboTipo, TextField txtAno, Label lblMensagem);

    // Apaga o item selecionado do banco após confirmação do usuário
    boolean acaoExcluir(TableView<LinguagemDTO> tableView, Label lblMensagem);

    // Limpa todas as caixas de texto e reseta o estado dos botões
    void limparCamposVisuais(TextField txtNome, TextField txtCriador, TextField txtAno, ComboBox<String> comboTipo, TableView<LinguagemDTO> tableView, Button btnSave, Button btnClear, Label lblMensagem);
}