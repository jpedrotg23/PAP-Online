package com.br.unifor.paponline;

import com.br.unifor.paponline.dto.AdministradorResponse;
import com.br.unifor.paponline.dto.AlunoResponse;
import com.br.unifor.paponline.dto.ProfissionalResponse;
import com.br.unifor.paponline.entidades.Administrador;
import com.br.unifor.paponline.entidades.Aluno;
import com.br.unifor.paponline.entidades.Profissional;
import com.br.unifor.paponline.config.JacksonConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;

class UserResponseSerializationTest {

    private final ObjectMapper objectMapper = new JacksonConfig().objectMapper();

    @Test
    void userResponseDtosAndEntitiesNeverSerializePasswordHashes() throws Exception {
        Aluno aluno = new Aluno();
        aluno.setSenha("$2a$12$private-hash");
        Profissional profissional = new Profissional();
        profissional.setSenha("$2a$12$private-hash");
        Administrador administrador = new Administrador();
        administrador.setSenha("$2a$12$private-hash");

        assertNoPassword(objectMapper.writeValueAsString(AlunoResponse.from(aluno)));
        assertNoPassword(objectMapper.writeValueAsString(ProfissionalResponse.from(profissional)));
        assertNoPassword(objectMapper.writeValueAsString(AdministradorResponse.from(administrador)));
        assertNoPassword(objectMapper.writeValueAsString(aluno));
        assertNoPassword(objectMapper.writeValueAsString(profissional));
        assertNoPassword(objectMapper.writeValueAsString(administrador));
    }

    private void assertNoPassword(String json) {
        assertFalse(json.contains("\"senha\""));
        assertFalse(json.contains("$2a$12$private-hash"));
    }
}
