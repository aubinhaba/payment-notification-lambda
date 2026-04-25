package com.bino.payment.notifier.exception;

public class StripeSignatureException extends RuntimeException {

    public StripeSignatureException(String message) {
        super(message);
    }

    public StripeSignatureException(String message, Throwable cause) {
        super(message, cause);
    }
}
