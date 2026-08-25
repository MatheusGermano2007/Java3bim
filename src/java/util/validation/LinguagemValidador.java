package util.validation;

import util.DialogUtil;
import java.util.ArrayList;
import java.util.List;

public class LinguagemValidador {

    public boolean validarCadastro(String nome, String criador, String ano) {

        List<Validador<String>> validadores = new ArrayList<>();


        // Usamos a mesma classe para checar se algum dos três textos está em branco
        validadores.add(new CampoObrigatorioValidador("Nome", nome));
        validadores.add(new CampoObrigatorioValidador("Criador", criador));
        validadores.add(new CampoObrigatorioValidador("Ano", ano));


        // Usamos a classe do ano para checar se digitaram letras no lugar de números
        validadores.add(new AnoNumericoValidador(ano));

        // Roda o teste um por um
        for (Validador<String> validador : validadores) {
            if (!validador.validar(validador.getValor())) {
                DialogUtil.showError(validador.getMensagemErro());
                return false;
            }
        }
        return true;
    }
}