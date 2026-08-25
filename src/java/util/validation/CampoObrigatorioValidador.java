package util.validation;

// regra pra nao deixar o cara salvar com campo em branco
public class CampoObrigatorioValidador implements Validador<String> {
    private final String nomeCampo;
    private final String valor;

    // recebe o nome do campo e o texto que o usuario digitou
    public CampoObrigatorioValidador(String nomeCampo, String valor) {
        this.nomeCampo = nomeCampo;
        this.valor = valor;
    }

    @Override
    // avisa que ta falso se tiver nulo ou so com espacos
    public boolean validar(String valorAtual) {
        return this.valor != null && !this.valor.trim().isEmpty();
    }

    @Override
    // monta o texto de erro usando o nome do campo
    public String getMensagemErro() {
        return "O campo " + nomeCampo + " deve ser preenchido.";
    }

    @Override
    // so retorna o texto que foi guardado la em cima
    public String getValor() {
        return valor;
    }
}