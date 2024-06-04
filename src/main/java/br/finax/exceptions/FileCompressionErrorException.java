package br.finax.exceptions;

public class FileCompressionErrorException extends RuntimeException {
    public FileCompressionErrorException() {
        super();
    }

    public FileCompressionErrorException(String msg) {
        super(msg);
    }
}
