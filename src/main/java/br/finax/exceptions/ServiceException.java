package br.finax.exceptions;

import br.finax.enums.ErrorCategory;
import lombok.Getter;

@Getter
public class ServiceException extends RuntimeException {

    private final ErrorCategory errorCategory;

    public ServiceException(ErrorCategory errorCategory, String message) {
        super(message);
        this.errorCategory = errorCategory;
    }

    public ServiceException(ErrorCategory errorCategory, String message, Throwable cause) {
        super(message, cause);
        this.errorCategory = errorCategory;
    }
}
