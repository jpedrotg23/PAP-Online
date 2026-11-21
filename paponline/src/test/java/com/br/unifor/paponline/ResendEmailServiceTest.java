package com.br.unifor.paponline;

import com.br.unifor.paponline.service.EmailDeliveryException;
import com.br.unifor.paponline.service.ResendEmailService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.net.SocketTimeoutException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class ResendEmailServiceTest {

    @Test
    void sendsRecoveryEmailThroughResendHttpApi() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        server.expect(requestTo("https://api.resend.com/emails"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header("Authorization", "Bearer test-only-key"))
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(content().json("""
                        {
                          "from": "PAP <pap@example.test>",
                          "to": "student@example.test",
                          "subject": "Código de recuperação de senha - PAP Online",
                          "text": "Seu código de recuperação é 123456. Ele expira em 15 minutos."
                        }
                        """))
                .andRespond(withSuccess("{\"id\":\"simulated\"}", MediaType.APPLICATION_JSON));

        new ResendEmailService(builder, "test-only-key", "PAP <pap@example.test>")
                .enviarCodigoRecuperacao("student@example.test", "123456");

        server.verify();
    }

    @Test
    void failsClosedWhenEmailProviderIsNotConfigured() {
        ResendEmailService service = new ResendEmailService(RestClient.builder(), "", "");
        EmailDeliveryException exception = assertThrows(EmailDeliveryException.class,
                () -> service.enviarCodigoRecuperacao("student@example.test", "123456"));

        assertEquals("resend_configuration_missing", exception.getDiagnosticCode());
        assertNull(exception.getCause());
    }

    @Test
    void reportsMissingConfigurationWithoutExposingValues() {
        EmailDeliveryException missingKey = assertThrows(EmailDeliveryException.class,
                () -> new ResendEmailService(RestClient.builder(), " ", "PAP <pap@example.test>")
                        .enviarCodigoRecuperacao("student@example.test", "123456"));
        EmailDeliveryException missingSender = assertThrows(EmailDeliveryException.class,
                () -> new ResendEmailService(RestClient.builder(), "test-only-key", " ")
                        .enviarCodigoRecuperacao("student@example.test", "123456"));

        assertEquals("resend_configuration_missing", missingKey.getDiagnosticCode());
        assertEquals("resend_configuration_missing", missingSender.getDiagnosticCode());
        assertEquals("O serviço de e-mail não está configurado.", missingKey.getMessage());
        assertEquals("O serviço de e-mail não está configurado.", missingSender.getMessage());
    }

    @ParameterizedTest
    @ValueSource(ints = {401, 403, 422, 429, 500, 503})
    void preservesSafeDiagnosticForResendHttpFailures(int statusCode) {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer.bindTo(builder).build()
                .expect(requestTo("https://api.resend.com/emails"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(org.springframework.test.web.client.response.MockRestResponseCreators
                        .withStatus(HttpStatus.valueOf(statusCode))
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("{\"message\":\"private provider detail\"}"));

        EmailDeliveryException exception = assertThrows(EmailDeliveryException.class,
                () -> new ResendEmailService(builder, "test-only-key", "PAP <pap@example.test>")
                        .enviarCodigoRecuperacao("student@example.test", "123456"));

        assertEquals("resend_http_" + statusCode, exception.getDiagnosticCode());
        assertNotNull(exception.getCause());
        assertEquals("Não foi possível enviar o e-mail de recuperação.", exception.getMessage());
    }

    @Test
    void preservesSafeDiagnosticForConnectionTimeout() throws Exception {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer.bindTo(builder).build()
                .expect(requestTo("https://api.resend.com/emails"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(org.springframework.test.web.client.response.MockRestResponseCreators
                        .withException(new SocketTimeoutException("sensitive timeout detail")));

        EmailDeliveryException exception = assertThrows(EmailDeliveryException.class,
                () -> new ResendEmailService(builder, "test-only-key", "PAP <pap@example.test>")
                        .enviarCodigoRecuperacao("student@example.test", "123456"));

        assertEquals("resend_connection_failure", exception.getDiagnosticCode());
        assertNotNull(exception.getCause());
        assertEquals("Não foi possível enviar o e-mail de recuperação.", exception.getMessage());
    }
}
