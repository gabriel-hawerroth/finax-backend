package br.finax.exceptions;

public class CannotChangePasswordException extends RuntimeException {
    public CannotChangePasswordException() {
        super();
    }

    public CannotChangePasswordException(String msg) {
        super(msg);
    }
}
