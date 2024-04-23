package br.finax.exceptions;

public class InactiveUserException extends RuntimeException {
    public InactiveUserException() {
        super();
    }

    public InactiveUserException(String msg) {
        super(msg);
    }
}
