package br.finax.exceptions;

public class ExpiredLinkException extends RuntimeException {
    public ExpiredLinkException() {
        super("The link has expired");
    }
}
