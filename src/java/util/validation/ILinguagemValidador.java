package util.validation;

public interface ILinguagemValidador {
    boolean validarCadastro(String nome, String criador, String ano);
}