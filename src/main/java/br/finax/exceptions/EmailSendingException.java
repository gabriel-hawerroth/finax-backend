package br.finax.exceptions;

public class EmailSendingException extends RuntimeException {

    public EmailSendingException(Throwable throwable) {
        super(throwable);
    }
}
