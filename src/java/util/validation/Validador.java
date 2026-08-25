package util.validation;

// contrato padrao que toda classe de regra vai ter que seguir
public interface Validador<T> {
    // testa o valor e diz se passou ou nao
    boolean validar(T valorAtual);

    // devolve o aviso caso a validacao falhe
    String getMensagemErro();

    // devolve o dado que a gente ta testando
    T getValor();
}