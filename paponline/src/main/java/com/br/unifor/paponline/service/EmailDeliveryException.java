package com.br.unifor.paponline.service;

public class EmailDeliveryException extends RuntimeException {
    private final String diagnosticCode;

    public EmailDeliveryException(String message) {
        this(message, "email_delivery_failure", null);
    }

    public EmailDeliveryException(String message, String diagnosticCode, Throwable cause) {
        super(message, cause);
        this.diagnosticCode = diagnosticCode;
    }

    public String getDiagnosticCode() {
        return diagnosticCode;
    }
}
