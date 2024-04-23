package br.finax.exceptions;

public class InvalidPasswordException extends RuntimeException {
    public InvalidPasswordException() {
        super();
    }

    public InvalidPasswordException(String msg) {
        super(msg);
    }
}
