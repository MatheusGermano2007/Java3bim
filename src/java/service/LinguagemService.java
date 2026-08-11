package service;

import model.dao.LinguagemDAO;
import model.dto.LinguagemDTO;
import java.util.List;

public class LinguagemService {

    private final LinguagemDAO dao;

    public LinguagemService() {
        this.dao = new LinguagemDAO();
    }

    public void cadastrarLinguagem(LinguagemDTO dto) throws Exception {
        dao.cadastrarLinguagem(dto);
    }

    public List<LinguagemDTO> listarLinguagens() throws Exception {
        return dao.listarLinguagens();
    }

    public void atualizarLinguagem(LinguagemDTO dto) throws Exception {
        dao.atualizarLinguagem(dto);
    }

    public void excluirLinguagem(int id) throws Exception {
        dao.excluirLinguagem(id);
    }
}