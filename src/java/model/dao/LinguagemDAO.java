package model.dao;

import model.ConexaoBD;
import model.dto.LinguagemDTO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class LinguagemDAO {

    // CADASTRO
    public void cadastrarLinguagem(LinguagemDTO objLinguagem) {
        String sql = "INSERT INTO linguagens (nome, criador, tipo, ano_criacao) VALUES (?, ?, ?, ?)";

        // try-with-resources: Abre a conexão e o statement. Fecha tudo sozinho no final
        try (Connection conn = new ConexaoBD().conectarBD();
             PreparedStatement pstm = conn.prepareStatement(sql)) {

            // Substitui as (?) pelos dados da tela
            pstm.setString(1, objLinguagem.getNome());
            pstm.setString(2, objLinguagem.getCriador());
            pstm.setString(3, objLinguagem.getTipo());
            pstm.setInt(4, objLinguagem.getAnoCriacao());

            pstm.execute(); // Executa no banco

        } catch (SQLException e) {
            // Se der erro, mostra no console do IntelliJ
            System.out.println("Erro ao cadastrar linguagem: " + e.getMessage());
        }
    }

    // LISTAR
    public List<LinguagemDTO> listarLinguagens() {
        String sql = "SELECT * FROM linguagens";
        List<LinguagemDTO> lista = new ArrayList<>();

        // try-with-resources para Connection, PreparedStatement e ResultSet
        try (Connection conn = new ConexaoBD().conectarBD();
             PreparedStatement pstm = conn.prepareStatement(sql);
             ResultSet rs = pstm.executeQuery()) { // Executa e recebe os dados

            // Lê os resultados linha por linha
            while (rs.next()) {
                LinguagemDTO objLinguagem = new LinguagemDTO();

                // Preenche o objeto
                objLinguagem.setId(rs.getInt("id_linguagem"));
                objLinguagem.setNome(rs.getString("nome"));
                objLinguagem.setCriador(rs.getString("criador"));
                objLinguagem.setTipo(rs.getString("tipo"));
                objLinguagem.setAnoCriacao(rs.getInt("ano_criacao"));

                lista.add(objLinguagem); // Adiciona na lista final
            }

        } catch (SQLException e) {
            System.out.println("Erro ao listar linguagens: " + e.getMessage());
        }

        return lista; // Devolve os dados prontos para a tela
    }

    //  EXCLUSÃO
    public void excluirLinguagem(int idLinguagem) {
        String sql = "DELETE FROM linguagens WHERE id_linguagem = ?";

        try (Connection conn = new ConexaoBD().conectarBD();
             PreparedStatement pstm = conn.prepareStatement(sql)) {

            // Passa o ID selecionado na tela para a (?)
            pstm.setInt(1, idLinguagem);
            pstm.execute(); // Deleta do banco

        } catch (SQLException e) { // Alterado de Exception para SQLException (mais específico e correto)
            System.out.println("Erro ao excluir linguagem: " + e.getMessage());
        }
    }

    //  ATUALIZAR
    public void atualizarLinguagem(LinguagemDTO objLinguagem) {
        // SQL de atualização. Atualiza apenas a linguagem que tiver o ID correspondente
        String sql = "UPDATE linguagens SET nome = ?, criador = ?, tipo = ?, ano_criacao = ? WHERE id_linguagem = ?";

        try (Connection conn = new ConexaoBD().conectarBD();
             PreparedStatement pstm = conn.prepareStatement(sql)) {

            // Substitui as (?) pelos novos dados
            pstm.setString(1, objLinguagem.getNome());
            pstm.setString(2, objLinguagem.getCriador());
            pstm.setString(3, objLinguagem.getTipo());
            pstm.setInt(4, objLinguagem.getAnoCriacao());
            pstm.setInt(5, objLinguagem.getId()); // O ID usado no WHERE

            pstm.execute(); // Executa a alteração no banco

        } catch (SQLException e) {
            System.out.println("Erro ao atualizar linguagem: " + e.getMessage());
        }
    }
}