package util;

import model.dto.LinguagemDTO;

// Classe responsável por concentrar as validações da tela de cadastro de linguagens.
// Tira do Controller a responsabilidade de decidir "o que é válido" -
// o Controller só chama esse validador e reage ao resultado.
public class LinguagemValidador {

    // Verifica se algum dos campos obrigatórios está vazio.
    // Usado para decidir se o botão SALVAR deve ficar habilitado.
    public static boolean algumCampoVazio(String nome, String criador, String ano) {
        return nome.trim().isEmpty() ||
                criador.trim().isEmpty() ||
                ano.trim().isEmpty();
    }

    // Verifica se TODOS os campos estão vazios.
    // Usado para decidir se o botão LIMPAR deve ficar habilitado.
    public static boolean todosCamposVazios(String nome, String criador, String ano) {
        return nome.trim().isEmpty() &&
                criador.trim().isEmpty() &&
                ano.trim().isEmpty();
    }

    // Tenta converter o texto do ano para número inteiro.
    // Retorna null se o texto não for um número válido, evitando que o
    // Controller precise lidar diretamente com NumberFormatException.
    public static Integer converterAno(String textoAno) {
        try {
            return Integer.parseInt(textoAno.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    // Compara os dados atualmente digitados na tela com os dados originais
    // do objeto selecionado na tabela, para saber se houve alguma alteração.
    public static boolean semAlteracoes(LinguagemDTO original, String nomeTela, String criadorTela,
                                        String tipoTela, int anoTela) {
        return nomeTela.equals(original.getNome()) &&
                criadorTela.equals(original.getCriador()) &&
                tipoTela.equals(original.getTipo()) &&
                anoTela == original.getAnoCriacao();
    }
}