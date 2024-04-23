package br.finax.exceptions;

public class TokenCreationException extends RuntimeException {
    public TokenCreationException() {
        super();
    }

    public TokenCreationException(String msg) {
        super(msg);
    }
}
