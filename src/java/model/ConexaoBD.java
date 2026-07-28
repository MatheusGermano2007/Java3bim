package model;

import java.sql.Connection;    // Objeto que mantém a conexão aberta com o banco
import java.sql.DriverManager; // Gerenciador que faz a ligação
import java.sql.SQLException;

public class ConexaoBD {

    // Boas práticas: Usar constantes (static final) e separar as credenciais da URL
    private static final String URL = "jdbc:postgresql://localhost:5432/DB_linguagenscrud";
    private static final String USER = "postgres";
    private static final String PASSWORD = "postgres";

    // Método responsável por abrir a porta do PostgreSQL
    public Connection conectarBD() {
        try {
            // Tenta conectar usando os dados separados e já devolve a conexão direto
            return DriverManager.getConnection(URL, USER, PASSWORD);

        } catch (SQLException e) {
            // Lançar a exceção é melhor do que retornar null.
            // Se o banco não conectar, o programa deve falhar e avisar o motivo exato.
            throw new RuntimeException("Erro fatal ao conectar com o banco de dados: " + e.getMessage(), e);
        }
    }
}