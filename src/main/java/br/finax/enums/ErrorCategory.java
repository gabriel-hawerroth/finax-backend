package br.finax.enums;

import lombok.Getter;

@Getter
public enum ErrorCategory {

    SEE_OTHER(303), // Redirect to another resource
    BAD_REQUEST(400), // Request invalid format
    UNAUTHORIZED(401), // Unauthorized access
    FORBIDDEN(403), // Forbidden access
    NOT_FOUND(404), // Resource not found
    GONE(410), // Resource no longer available
    UNPROCESSABLE(422), // Request cannot be processed
    INTERNAL_ERROR(500), // Internal server error
    NOT_IMPLEMENTED(501), // Not implemented
    BAD_GATEWAY(502), // Bad gateway
    SERVICE_UNAVAILABLE(503); // Service unavailable

    private final int httpStatusCode;

    ErrorCategory(int httpStatusCode) {
        this.httpStatusCode = httpStatusCode;
    }
}
