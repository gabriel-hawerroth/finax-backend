package br.finax.exceptions;

public class CompressionErrorException extends RuntimeException {
    public CompressionErrorException() {
        super();
    }

    public CompressionErrorException(String msg) {
        super(msg);
    }
}
