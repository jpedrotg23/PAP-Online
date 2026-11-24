package com.br.unifor.paponline;

import com.br.unifor.paponline.dto.RelatorioResumoResponse;
import org.junit.jupiter.api.Test;

import java.lang.reflect.RecordComponent;
import java.util.Arrays;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;

class RelatorioResumoResponseTest {

    @Test
    void dtoENestedRecordsNaoContemComponentesPessoaisDeAluno() {
        Set<String> camposPessoais = Set.of("email", "matricula", "telefone", "alunoid", "alunonome");
        Class<?>[] records = Arrays.copyOf(
                RelatorioResumoResponse.class.getDeclaredClasses(),
                RelatorioResumoResponse.class.getDeclaredClasses().length + 1);
        records[records.length - 1] = RelatorioResumoResponse.class;

        for (Class<?> record : records) {
            for (RecordComponent componente : record.getRecordComponents()) {
                assertFalse(camposPessoais.contains(componente.getName().toLowerCase()),
                        record.getSimpleName() + " contém o campo pessoal " + componente.getName());
            }
        }
    }
}
