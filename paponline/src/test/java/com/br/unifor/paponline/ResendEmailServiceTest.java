package com.br.unifor.paponline;

import com.br.unifor.paponline.service.EmailDeliveryException;
import com.br.unifor.paponline.service.ResendEmailService;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
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
                .andExpect(content().string(org.hamcrest.Matchers.containsString("123456")))
                .andRespond(withSuccess("{\"id\":\"simulated\"}", MediaType.APPLICATION_JSON));

        new ResendEmailService(builder, "test-only-key", "PAP <pap@example.test>")
                .enviarCodigoRecuperacao("student@example.test", "123456");

        server.verify();
    }

    @Test
    void failsClosedWhenEmailProviderIsNotConfigured() {
        ResendEmailService service = new ResendEmailService(RestClient.builder(), "", "");
        assertThrows(EmailDeliveryException.class,
                () -> service.enviarCodigoRecuperacao("student@example.test", "123456"));
    }
}
