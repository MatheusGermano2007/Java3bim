package util.validation;

// regra pra garantir que digitaram um numero e nao letras no ano
public class AnoNumericoValidador implements Validador<String> {
    private final String valor;

    // guarda o texto do ano que veio da tela
    public AnoNumericoValidador(String valor) {
        this.valor = valor;
    }

    @Override
    // tenta transformar o texto em numero inteiro
    public boolean validar(String valorAtual) {
        // se ja tiver vazio nem tenta e devolve falso
        if (this.valor == null || this.valor.trim().isEmpty()) return false;
        try {
            // tenta converter e se der bom passa
            Integer.parseInt(this.valor);
            return true;
        } catch (NumberFormatException e) {
            // se der ruim na conversao bloqueia
            return false;
        }
    }

    @Override
    // recado padrao avisando que o ano ta invalido
    public String getMensagemErro() {
        return "O ano deve ser um número válido.";
    }

    @Override
    // devolve o texto do ano salvo
    public String getValor() {
        return valor;
    }
}