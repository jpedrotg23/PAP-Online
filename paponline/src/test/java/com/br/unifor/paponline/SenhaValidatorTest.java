package com.br.unifor.paponline;

import com.br.unifor.paponline.util.SenhaValidator;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SenhaValidatorTest {

    @Test
    void acceptsValidPassword() {
        assertDoesNotThrow(() -> SenhaValidator.exigirValida("Teste1234"));
    }

    @Test
    void rejectsPasswordsThatViolateAnyStrengthRequirement() {
        assertThrows(IllegalArgumentException.class, () -> SenhaValidator.exigirValida("Teste1"));
        assertThrows(IllegalArgumentException.class, () -> SenhaValidator.exigirValida("teste1234"));
        assertThrows(IllegalArgumentException.class, () -> SenhaValidator.exigirValida("TESTE1234"));
        assertThrows(IllegalArgumentException.class, () -> SenhaValidator.exigirValida("Testeabcd"));
        assertThrows(IllegalArgumentException.class, () -> SenhaValidator.exigirValida(null));
        assertThrows(IllegalArgumentException.class, () -> SenhaValidator.exigirValida(""));
    }
}
