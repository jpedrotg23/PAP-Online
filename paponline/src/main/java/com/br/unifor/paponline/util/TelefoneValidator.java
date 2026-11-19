package com.br.unifor.paponline.util;

/**
 * Valida telefone brasileiro: 10 dígitos (fixo) ou 11 (celular), com DDD 11-99.
 * Fixo: primeiro dígito após DDD ∈ {2,3,4,5}
 * Celular: primeiro dígito após DDD = 9
 * Ignora a máscara: (85) 99999-9999 → 85999999999
 */
public final class TelefoneValidator {

    private TelefoneValidator() {}

    public static void exigirValido(String telefone) {
        if (telefone == null || telefone.isBlank()) {
            throw new IllegalArgumentException("Informe um telefone.");
        }

        String digits = telefone.replaceAll("\\D", "");

        if (digits.length() != 10 && digits.length() != 11) {
            throw new IllegalArgumentException("Telefone deve ter 10 ou 11 dígitos com DDD.");
        }

        int ddd = Integer.parseInt(digits.substring(0, 2));
        if (ddd < 11 || ddd > 99) {
            throw new IllegalArgumentException("DDD inválido (use 11 a 99).");
        }

        char primeiro = digits.charAt(2);

        if (digits.length() == 11) {
            if (primeiro != '9') {
                throw new IllegalArgumentException("Celular deve começar com 9 após o DDD.");
            }
        } else {
            if (primeiro != '2' && primeiro != '3' && primeiro != '4' && primeiro != '5') {
                throw new IllegalArgumentException("Telefone fixo deve começar com 2, 3, 4 ou 5 após o DDD.");
            }
        }
    }
}