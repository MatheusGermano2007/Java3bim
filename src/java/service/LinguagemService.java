package service;

import javafx.collections.FXCollections;
import javafx.collections.transformation.FilteredList;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import model.dao.LinguagemDAO;
import model.dto.LinguagemDTO;
import util.DialogUtil;
import util.validation.AnoNumericoValidador;
import util.validation.CampoObrigatorioValidador;
import util.validation.Validador;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
            bSave.setDisable(tem);

            if (tem) {
                tNome.setText(sel.getNome());
                tCriador.setText(sel.getCriador());
                combo.setValue(sel.getTipo());
                tAno.setText(String.valueOf(sel.getAnoCriacao()));
            }
        });

        // Validador em tempo real usando as classes OCP do professor
        javafx.beans.value.ChangeListener<String> validadorUI = (obs, old, novo) -> {
            boolean nomeValido = new CampoObrigatorioValidador("Nome", tNome.getText()).validar(tNome.getText());
            boolean criadorValido = new CampoObrigatorioValidador("Criador", tCriador.getText()).validar(tCriador.getText());
            boolean anoValido = new CampoObrigatorioValidador("Ano", tAno.getText()).validar(tAno.getText());

            boolean formInvalido = !(nomeValido && criadorValido && anoValido);
            boolean temSelecaoNaTabela = table.getSelectionModel().getSelectedItem() != null;

            bSave.setDisable(formInvalido || temSelecaoNaTabela);

            boolean tudoVazio = tNome.getText().isEmpty() && tCriador.getText().isEmpty() && tAno.getText().isEmpty();
            bClear.setDisable(tudoVazio);
        };

        Arrays.asList(tNome, tCriador, tAno).forEach(txt -> txt.textProperty().addListener(validadorUI));
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

        // Chama exatamente a classe que acabamos de criar acima
        LinguagemValidador validador = new LinguagemValidador();

        // Se esbarrar em campo vazio ou ano com letras, ele já para aqui
        if (!validador.validarCadastro(tNome.getText(), tCriador.getText(), tAno.getText())) {
            return false;
        }

        // Se passou, salva no banco de dados!
        try {
            Integer ano = Integer.parseInt(tAno.getText());
            dao.cadastrarLinguagem(new LinguagemDTO(tNome.getText(), tCriador.getText(), combo.getValue(), ano));

            DialogUtil.showInfo("Salvo com sucesso!");
            atualizarLabel(lMsg, "Linguagem salva!", "#00ff00", true);
            return true;
        } catch (Exception e) {
            DialogUtil.showError("Erro: " + e.getMessage());
            return false;
        }
    }

    public boolean acaoAtualizar(TableView<LinguagemDTO> table, TextField tNome, TextField tCriador, ComboBox<String> combo, TextField tAno, Label lMsg) {
        LinguagemDTO dto = table.getSelectionModel().getSelectedItem();
        if (dto == null) {
            atualizarLabel(lMsg, "Nenhum item selecionado.", "#ff4c4c", true);
            return false;
        }

        // Padrão do Professor: Construindo a lista de validadores para atualizar também
        List<Validador<String>> validadores = new ArrayList<>();
        validadores.add(new CampoObrigatorioValidador("Nome", tNome.getText()));
        validadores.add(new CampoObrigatorioValidador("Criador", tCriador.getText()));
        validadores.add(new CampoObrigatorioValidador("Ano", tAno.getText()));
        validadores.add(new AnoNumericoValidador(tAno.getText()));

        for (Validador<String> validador : validadores) {
            if (!validador.validar(validador.getValor())) {
                atualizarLabel(lMsg, validador.getMensagemErro(), "#ff4c4c", true);
                return false;
            }
        }

        try {
            Integer ano = Integer.parseInt(tAno.getText());

            // Verifica se algo foi alterado de fato
            if (dto.getNome().equals(tNome.getText()) &&
                    dto.getCriador().equals(tCriador.getText()) &&
                    dto.getTipo().equals(combo.getValue()) &&
                    dto.getAnoCriacao() == ano) {

                atualizarLabel(lMsg, "Nenhuma alteração feita.", "#ff4c4c", true);
                return false;
            }

            dto.setNome(tNome.getText());
            dto.setCriador(tCriador.getText());
            dto.setTipo(combo.getValue());
            dto.setAnoCriacao(ano);

            dao.atualizarLinguagem(dto);
            atualizarLabel(lMsg, "Atualizado com sucesso!", "#00ff00", true);
            return true;
        } catch (Exception e) {
            atualizarLabel(lMsg, "Erro: " + e.getMessage(), "#ff4c4c", true);
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

    private void atualizarLabel(Label lbl, String txt, String cor, boolean bold) {
        if (lbl != null) { lbl.setText(txt); lbl.setStyle("-fx-text-fill: " + cor + ";" + (bold ? " -fx-font-weight: bold;" : "")); }
    }
}