package br.finax.exceptions;

public class UnsendedEmailException extends RuntimeException {
    public UnsendedEmailException() {
        super();
    }

    public UnsendedEmailException(String msg) {
        super(msg);
    }
}
