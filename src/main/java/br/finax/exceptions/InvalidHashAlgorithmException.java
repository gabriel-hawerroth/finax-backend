package br.finax.exceptions;

public class InvalidHashAlgorithmException extends RuntimeException {
    public InvalidHashAlgorithmException() {
        super();
    }

    public InvalidHashAlgorithmException(String msg) {
        super(msg);
    }
}
