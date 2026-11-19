package com.br.unifor.paponline.util;

import java.util.regex.Pattern;

public final class SenhaValidator {

    private static final Pattern MAIUSCULA = Pattern.compile("[A-Z]");
    private static final Pattern MINUSCULA = Pattern.compile("[a-z]");
    private static final Pattern NUMERO    = Pattern.compile("\\d");

    private SenhaValidator() {}

    public static boolean valida(String senha) {
        return senha != null
                && senha.length() >= 8
                && MAIUSCULA.matcher(senha).find()
                && MINUSCULA.matcher(senha).find()
                && NUMERO.matcher(senha).find();
    }

    public static void exigirValida(String senha) {
        if (!valida(senha)) {
            throw new IllegalArgumentException(
                    "Senha deve ter 8+ caracteres, com maiúscula, minúscula e número.");
        }
    }
}