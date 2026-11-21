package com.br.unifor.paponline.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import java.util.Map;

@Slf4j
@Service
public class ResendEmailService {

    private final RestClient restClient;
    private final String apiKey;
    private final String from;

    public ResendEmailService(
            RestClient.Builder builder,
            @Value("${RESEND_API_KEY:}") String apiKey,
            @Value("${PAP_EMAIL_FROM:}") String from) {
        this.restClient = builder.baseUrl("https://api.resend.com").build();
        this.apiKey = apiKey;
        this.from = from;
    }

    public void enviarCodigoRecuperacao(String destinatario, String codigo) {
        if (apiKey.isBlank() || from.isBlank()) {
            throw new EmailDeliveryException("O serviço de e-mail não está configurado.");
        }
        Map<String, Object> body = Map.of(
                "from", from,
                "to", destinatario,
                "subject", "Código de recuperação de senha - PAP Online",
                "text", "Seu código de recuperação é " + codigo + ". Ele expira em 15 minutos."
        );
        try {
            restClient.post()
                    .uri("/emails")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                    .body(body)
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientException ex) {
            log.warn("Falha operacional no envio de e-mail de recuperação ({})",
                    ex.getClass().getSimpleName());
            throw new EmailDeliveryException("Não foi possível enviar o e-mail de recuperação.");
        }
    }
}
