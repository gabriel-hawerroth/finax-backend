package br.finax.exceptions;

public class EmptyFileException extends RuntimeException {
    public EmptyFileException() {
        super();
    }

    public EmptyFileException(String msg) {
        super(msg);
    }
}
