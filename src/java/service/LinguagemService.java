package service;

import javafx.collections.FXCollections;
import javafx.collections.transformation.FilteredList;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import model.dao.LinguagemDAO;
import model.dto.LinguagemDTO;
import util.DialogUtil;
import util.LinguagemValidador;
import java.util.Arrays;

public class LinguagemService {

    private final LinguagemDAO dao = new LinguagemDAO();

    // --- CONFIGURAÇÕES VISUAIS DA TELA ---

    public void inicializarTabela(TableView<LinguagemDTO> table, TableColumn<LinguagemDTO, String> cNome, TableColumn<LinguagemDTO, String> cCriador, TableColumn<LinguagemDTO, String> cTipo, TableColumn<LinguagemDTO, String> cAno, ComboBox<String> combo) {
        cNome.setCellValueFactory(new PropertyValueFactory<>("nome"));
        cCriador.setCellValueFactory(new PropertyValueFactory<>("criador"));
        cTipo.setCellValueFactory(new PropertyValueFactory<>("tipo"));
        cAno.setCellValueFactory(new PropertyValueFactory<>("anoCriacao"));
        combo.getItems().addAll("Orientado a Objetos", "Funcional", "Backend", "Frontend");
        table.setPlaceholder(new Label("Nenhuma linguagem encontrada"));
    }

    public void configurarEventos(TableView<LinguagemDTO> table, TextField tNome, TextField tCriador, ComboBox<String> combo, TextField tAno, Button bSave, Button bClear, Button bUpd, Button bDel, TextField tSearch, Label lTotal) {

        table.getSelectionModel().selectedItemProperty().addListener((obs, old, sel) -> {
            boolean tem = sel != null;
            bUpd.setDisable(!tem);
            bDel.setDisable(!tem);
            bSave.setDisable(tem); // Trava o botão salvar no modo edição

            if (tem) {
                tNome.setText(sel.getNome());
                tCriador.setText(sel.getCriador());
                combo.setValue(sel.getTipo());
                tAno.setText(String.valueOf(sel.getAnoCriacao()));
            }
        });

        javafx.beans.value.ChangeListener<String> validador = (obs, old, novo) -> {
            boolean algumVazio = LinguagemValidador.algumCampoVazio(tNome.getText(), tCriador.getText(), tAno.getText());
            boolean temSelecaoNaTabela = table.getSelectionModel().getSelectedItem() != null;

            bSave.setDisable(algumVazio || temSelecaoNaTabela);
            bClear.setDisable(LinguagemValidador.todosCamposVazios(tNome.getText(), tCriador.getText(), tAno.getText()));
        };

        Arrays.asList(tNome, tCriador, tAno).forEach(txt -> txt.textProperty().addListener(validador));
        if (tSearch != null) tSearch.textProperty().addListener((obs, old, novo) -> recarregarTabela(table, tSearch, lTotal));
    }

    public void limparCamposVisuais(TextField tNome, TextField tCriador, TextField tAno, ComboBox<String> combo, TableView<LinguagemDTO> table, Button bSave, Button bClear, Label lMsg) {
        Arrays.asList(tNome, tCriador, tAno).forEach(TextInputControl::clear);
        if (combo != null) combo.getSelectionModel().clearSelection();
        table.getSelectionModel().clearSelection();
        bSave.setDisable(true); bClear.setDisable(true);
        tNome.requestFocus();
        atualizarLabel(lMsg, "Sistema pronto.", "#888", false);
    }

    public void recarregarTabela(TableView<LinguagemDTO> table, TextField tSearch, Label lTotal) {
        try {
            String termo = tSearch != null ? tSearch.getText().toLowerCase() : "";
            FilteredList<LinguagemDTO> filtrada = new FilteredList<>(FXCollections.observableArrayList(dao.listarLinguagens()),
                    lang -> termo.isEmpty() || lang.getNome().toLowerCase().contains(termo) || lang.getCriador().toLowerCase().contains(termo));
            table.setItems(filtrada);
            if (lTotal != null) lTotal.setText("Total: " + filtrada.size());
        } catch (Exception e) { e.printStackTrace(); }
    }

    // --- AÇÕES PRINCIPAIS (BANCO + TELA) ---

    public boolean acaoSalvar(TextField tNome, TextField tCriador, ComboBox<String> combo, TextField tAno, Label lMsg) {
        try {
            dao.cadastrarLinguagem(new LinguagemDTO(tNome.getText(), tCriador.getText(), combo.getValue(), validarAno(tAno.getText())));
            DialogUtil.showInfo("Salvo com sucesso!");
            atualizarLabel(lMsg, "Linguagem salva!", "#00ff00", true);
            return true;
        } catch (Exception e) {
            DialogUtil.showError(e instanceof IllegalArgumentException ? e.getMessage() : "Erro: " + e.getMessage());
            return false;
        }
    }

    public boolean acaoAtualizar(TableView<LinguagemDTO> table, TextField tNome, TextField tCriador, ComboBox<String> combo, TextField tAno, Label lMsg) {
        try {
            LinguagemDTO dto = table.getSelectionModel().getSelectedItem();
            if (dto == null) throw new IllegalArgumentException("Nenhum item selecionado.");
            Integer ano = validarAno(tAno.getText());

            if (LinguagemValidador.semAlteracoes(dto, tNome.getText(), tCriador.getText(), combo.getValue(), ano)) {
                throw new IllegalArgumentException("Nenhuma alteração feita.");
            }

            dto.setNome(tNome.getText()); dto.setCriador(tCriador.getText()); dto.setTipo(combo.getValue()); dto.setAnoCriacao(ano);
            dao.atualizarLinguagem(dto);
            atualizarLabel(lMsg, "Atualizado com sucesso!", "#00ff00", true);
            return true;
        } catch (Exception e) {
            atualizarLabel(lMsg, e.getMessage(), "#ff4c4c", true);
            return false;
        }
    }

    public boolean acaoExcluir(TableView<LinguagemDTO> table, Label lMsg) {
        LinguagemDTO sel = table.getSelectionModel().getSelectedItem();
        if (sel != null && DialogUtil.showConfirmation("Excluir", "Apagar " + sel.getNome() + " permanentemente?")) {
            try {
                dao.excluirLinguagem(sel.getId());
                atualizarLabel(lMsg, "Excluído com sucesso!", "#00ff00", true);
                return true;
            } catch (Exception e) {
                atualizarLabel(lMsg, "Erro: " + e.getMessage(), "#ff4c4c", true);
                return false;
            }
        }
        return false;
    }

    // --- UTILITÁRIOS INTERNOS ---

    private Integer validarAno(String anoTxt) {
        Integer ano = LinguagemValidador.converterAno(anoTxt);
        if (ano == null) throw new IllegalArgumentException("O ano deve ser numérico.");
        return ano;
    }

    private void atualizarLabel(Label lbl, String txt, String cor, boolean bold) {
        if (lbl != null) { lbl.setText(txt); lbl.setStyle("-fx-text-fill: " + cor + ";" + (bold ? " -fx-font-weight: bold;" : "")); }
    }
}