package com.br.unifor.paponline;

import com.br.unifor.paponline.entidades.TokenRecuperacaoSenha;
import com.br.unifor.paponline.entidades.Usuario;
import com.br.unifor.paponline.config.ApiExceptionHandler;
import com.br.unifor.paponline.controller.AuthController;
import com.br.unifor.paponline.service.UsuarioService;
import com.br.unifor.paponline.repository.TokenRecuperacaoSenhaRepository;
import com.br.unifor.paponline.repository.UsuarioRepository;
import com.br.unifor.paponline.service.EmailDeliveryException;
import com.br.unifor.paponline.service.RecuperacaoSenhaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class RecuperacaoSenhaServiceTest {

    private static final ZoneId FUSO_PAP = ZoneId.of("America/Fortaleza");

    private TokenRecuperacaoSenhaRepository tokens;
    private UsuarioRepository usuarios;
    private PasswordEncoder encoder;
    private com.br.unifor.paponline.service.ResendEmailService email;
    private RecuperacaoSenhaService service;
    private Usuario usuario;

    @BeforeEach
    void setUp() {
        tokens = mock(TokenRecuperacaoSenhaRepository.class);
        usuarios = mock(UsuarioRepository.class);
        encoder = new BCryptPasswordEncoder(4);
        email = mock(com.br.unifor.paponline.service.ResendEmailService.class);
        service = new RecuperacaoSenhaService(tokens, usuarios, encoder, email);
        usuario = mock(Usuario.class);
        when(usuario.getEmail()).thenReturn("Student@Example.edu");
        when(usuario.getSenha()).thenReturn(encoder.encode("SenhaAtual1"));
        when(usuarios.findByEmailIgnoreCase("student@example.edu")).thenReturn(Optional.of(usuario));
        when(usuarios.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(tokens.findFirstByIdentificadorOrderByCriadoEmDesc("student@example.edu"))
                .thenReturn(Optional.empty());
        when(tokens.findByIdentificadorAndUsadoFalse("student@example.edu")).thenReturn(List.of());
    }

    @Test
    void sendsSixDigitCodeToNormalizedRegisteredEmailAndStoresOnlyHash() {
        service.solicitarCodigo("  STUDENT@EXAMPLE.EDU ");

        var captor = org.mockito.ArgumentCaptor.forClass(TokenRecuperacaoSenha.class);
        verify(tokens).save(captor.capture());
        TokenRecuperacaoSenha token = captor.getValue();
        assertEquals("student@example.edu", token.getIdentificador());
        assertTrue(token.getExpiraEm().isAfter(agoraPAP()));
        assertEquals(0, token.getTentativas());
        var codeCaptor = org.mockito.ArgumentCaptor.forClass(String.class);
        verify(email).enviarCodigoRecuperacao(eq("Student@Example.edu"), codeCaptor.capture());
        assertTrue(codeCaptor.getValue().matches("\\d{6}"));
        assertFalse(token.getCodigo().matches("\\d{6}"));
        assertTrue(encoder.matches(codeCaptor.getValue(), token.getCodigo()));
    }

    @Test
    void successfulRequestCodeCanBeValidated() {
        service.solicitarCodigo("student@example.edu");
        var savedToken = org.mockito.ArgumentCaptor.forClass(TokenRecuperacaoSenha.class);
        verify(tokens).save(savedToken.capture());
        var sentCode = org.mockito.ArgumentCaptor.forClass(String.class);
        verify(email).enviarCodigoRecuperacao(eq("Student@Example.edu"), sentCode.capture());
        when(tokens.findFirstByIdentificadorAndUsadoFalseOrderByCriadoEmDesc("student@example.edu"))
                .thenReturn(Optional.of(savedToken.getValue()));

        assertDoesNotThrow(() -> service.validarCodigo("student@example.edu", sentCode.getValue()));
    }

    @Test
    void wrongCodeIncrementsAttemptsAndFifthAttemptInvalidatesToken() {
        TokenRecuperacaoSenha token = token("123456");
        token.setTentativas(4);
        when(tokens.findFirstByIdentificadorAndUsadoFalseOrderByCriadoEmDesc("student@example.edu"))
                .thenReturn(Optional.of(token));

        assertThrows(ResponseStatusException.class, () -> service.validarCodigo("student@example.edu", "999999"));
        assertEquals(5, token.getTentativas());
        assertTrue(token.getUsado());
        verify(tokens).save(token);
    }

    @Test
    void expiredCodeIsInvalidated() {
        TokenRecuperacaoSenha token = token("123456");
        token.setExpiraEm(agoraPAP().minusSeconds(1));
        when(tokens.findFirstByIdentificadorAndUsadoFalseOrderByCriadoEmDesc("student@example.edu"))
                .thenReturn(Optional.of(token));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> service.validarCodigo("student@example.edu", "123456"));
        assertTrue(exception.getReason().contains("expirado"));
        assertTrue(token.getUsado());
    }

    @Test
    void usedCodeCannotBeReused() {
        when(tokens.findFirstByIdentificadorAndUsadoFalseOrderByCriadoEmDesc("student@example.edu"))
                .thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class,
                () -> service.validarCodigo("student@example.edu", "123456"));
    }

    @Test
    void resendInvalidatesPreviousCode() {
        TokenRecuperacaoSenha old = token("111111");
        old.setCriadoEm(agoraPAP().minusMinutes(2));
        when(tokens.findFirstByIdentificadorOrderByCriadoEmDesc("student@example.edu"))
                .thenReturn(Optional.of(old));
        when(tokens.findByIdentificadorAndUsadoFalse("student@example.edu")).thenReturn(List.of(old));

        service.solicitarCodigo("student@example.edu");

        assertTrue(old.getUsado());
        verify(tokens, times(2)).save(any(TokenRecuperacaoSenha.class));
    }

    @Test
    void requestInsideFrequencyWindowDoesNotSendAnotherEmail() {
        TokenRecuperacaoSenha recent = token("111111");
        recent.setCriadoEm(agoraPAP().minusSeconds(10));
        when(tokens.findFirstByIdentificadorOrderByCriadoEmDesc("student@example.edu"))
                .thenReturn(Optional.of(recent));

        service.solicitarCodigo("student@example.edu");

        verify(email, never()).enviarCodigoRecuperacao(anyString(), anyString());
        verify(tokens, never()).save(any(TokenRecuperacaoSenha.class));
    }

    @Test
    void formattedPhoneIsNormalizedAndDeliveryUsesStoredEmail() {
        when(usuario.getTelefone()).thenReturn("(85) 99999-9999");
        when(usuarios.findAll()).thenReturn(List.of(usuario));

        service.solicitarCodigo("(85) 99999-9999");

        verify(email).enviarCodigoRecuperacao(eq("Student@Example.edu"), anyString());
        var savedToken = org.mockito.ArgumentCaptor.forClass(TokenRecuperacaoSenha.class);
        verify(tokens).save(savedToken.capture());
        assertEquals("student@example.edu", savedToken.getValue().getIdentificador());
    }

    @Test
    void failedEmailDoesNotPersistARecoveryToken() {
        TokenRecuperacaoSenha previousToken = token("111111");
        previousToken.setCriadoEm(agoraPAP().minusMinutes(2));
        when(tokens.findFirstByIdentificadorOrderByCriadoEmDesc("student@example.edu"))
                .thenReturn(Optional.of(previousToken));
        when(tokens.findByIdentificadorAndUsadoFalse("student@example.edu"))
                .thenReturn(List.of(previousToken));
        doThrow(new EmailDeliveryException("provider unavailable"))
                .when(email).enviarCodigoRecuperacao(anyString(), anyString());

        assertThrows(EmailDeliveryException.class, () -> service.solicitarCodigo("student@example.edu"));
        assertTrue(previousToken.getUsado());
        verify(tokens).save(previousToken);
        verify(tokens, times(1)).findByIdentificadorAndUsadoFalse("student@example.edu");
        verify(tokens, times(1)).save(any(TokenRecuperacaoSenha.class));
    }

    @Test
    void incorrectCodeCannotBeUsedToResetThePassword() {
        TokenRecuperacaoSenha token = token("123456");
        when(tokens.findFirstByIdentificadorAndUsadoFalseOrderByCriadoEmDesc("student@example.edu"))
                .thenReturn(Optional.of(token));

        assertThrows(ResponseStatusException.class,
                () -> service.redefinir("student@example.edu", "999999", "NovaSenha2"));

        verify(usuario, never()).setSenha(anyString());
        verify(usuarios, never()).save(usuario);
    }

    @Test
    void resetStoresOnlyAnEncodedNewPasswordAndConsumesToken() {
        TokenRecuperacaoSenha token = token("123456");
        when(tokens.findFirstByIdentificadorAndUsadoFalseOrderByCriadoEmDesc("student@example.edu"))
                .thenReturn(Optional.of(token));

        service.redefinir("student@example.edu", "123456", "NovaSenha2");

        assertTrue(token.getUsado());
        verify(usuario).setSenha(argThat(hash -> hash != null
                && !hash.equals("NovaSenha2")
                && encoder.matches("NovaSenha2", hash)));
        verify(usuarios).save(usuario);
        verify(tokens).save(token);
    }

    @Test
    void samePasswordIsRejectedWithoutChangingAccount() {
        TokenRecuperacaoSenha token = token("123456");
        when(tokens.findFirstByIdentificadorAndUsadoFalseOrderByCriadoEmDesc("student@example.edu"))
                .thenReturn(Optional.of(token));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> service.redefinir("student@example.edu", "123456", "SenhaAtual1"));
        assertTrue(exception.getReason().contains("diferente"));
        verify(usuarios, never()).save(usuario);
    }

    @Test
    void differentPasswordUpdatesAccountAndConsumesCode() {
        TokenRecuperacaoSenha token = token("123456");
        when(tokens.findFirstByIdentificadorAndUsadoFalseOrderByCriadoEmDesc("student@example.edu"))
                .thenReturn(Optional.of(token));

        service.redefinir("student@example.edu", "123456", "NovaSenha2");

        assertTrue(token.getUsado());
        verify(usuarios).save(usuario);
        verify(usuario).setSenha(argThat(hash -> encoder.matches("NovaSenha2", hash)));
    }

    @Test
    void unknownAccountDoesNotSendOrPersistRecoveryData() {
        when(usuarios.findByEmailIgnoreCase("missing@example.edu")).thenReturn(Optional.empty());

        assertDoesNotThrow(() -> service.solicitarCodigo("missing@example.edu"));
        verify(email, never()).enviarCodigoRecuperacao(anyString(), anyString());
        verify(tokens, never()).save(any(TokenRecuperacaoSenha.class));
    }

    @Test
    void recoveryHttpResponseNeverContainsCode() {
        RecuperacaoSenhaService recovery = mock(RecuperacaoSenhaService.class);
        AuthController controller = new AuthController(mock(UsuarioService.class), recovery);

        Map<String, String> response = controller.recuperar(Map.of("identifier", "student@example.edu"));

        assertFalse(response.containsKey("codigoDemo"));
        assertFalse(response.values().stream().anyMatch(value -> value.matches("\\d{6}")));
        verify(recovery).solicitarCodigo("student@example.edu");
    }

    @Test
    void nonexistentAccountAndDeliveryFailureHaveIdenticalHttpResponses() throws Exception {
        RecuperacaoSenhaService recovery = mock(RecuperacaoSenhaService.class);
        AuthController controller = new AuthController(mock(UsuarioService.class), recovery);
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
        String requestBody = "{\"identifier\":\"student@example.edu\"}";

        MvcResult unknownAccount = mockMvc.perform(post("/auth/recuperar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andReturn();

        doThrow(new EmailDeliveryException(
                "provider error", "resend_http_422", new IllegalStateException("private detail")))
                .when(recovery).solicitarCodigo("student@example.edu");
        MvcResult failedDelivery = mockMvc.perform(post("/auth/recuperar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andReturn();

        assertEquals(unknownAccount.getResponse().getStatus(), failedDelivery.getResponse().getStatus());
        assertEquals(unknownAccount.getResponse().getContentAsString(),
                failedDelivery.getResponse().getContentAsString());
        assertEquals("""
                {"mensagem":"Se o endereço estiver cadastrado, você receberá as instruções de recuperação."}
                """.trim(), failedDelivery.getResponse().getContentAsString());
        assertFalse(failedDelivery.getResponse().getContentAsString().contains("private detail"));
        assertFalse(failedDelivery.getResponse().getContentAsString().contains("resend_http_422"));
    }

    @Test
    void emailFailureResponseDoesNotExposeCauseOrRecoveryCode() {
        var response = new ApiExceptionHandler().tratarFalhaDeEmail(
                new EmailDeliveryException("provider error 123456", "resend_http_422",
                        new IllegalStateException("private credential detail")));

        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, response.getStatusCode());
        String body = response.getBody().toString();
        assertFalse(body.contains("123456"));
        assertFalse(body.contains("private credential detail"));
        assertFalse(body.contains("resend_http_422"));
    }

    private TokenRecuperacaoSenha token(String rawCode) {
        TokenRecuperacaoSenha token = new TokenRecuperacaoSenha();
        token.setIdentificador("student@example.edu");
        token.setCodigo(encoder.encode(rawCode));
        token.setExpiraEm(agoraPAP().plusMinutes(15));
        token.setUsado(false);
        token.setTentativas(0);
        token.setCriadoEm(agoraPAP().minusMinutes(2));
        return token;
    }

    private static LocalDateTime agoraPAP() {
        return LocalDateTime.now(FUSO_PAP);
    }
}
