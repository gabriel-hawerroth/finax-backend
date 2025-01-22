package br.finax.config;

import br.finax.dto.ResponseError;
import br.finax.enums.ErrorCategory;
import br.finax.exceptions.*;
import br.finax.utils.ServiceUrls;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.net.URI;
import java.util.logging.Logger;

import static br.finax.utils.UtilsService.extractRelevantErrorMessage;

@RestControllerAdvice
@RequiredArgsConstructor
public class ExceptionHandlers {

    private static final String INTERNAL_ERROR = "An internal error has occurred";

    private final Logger logger = Logger.getLogger(this.getClass().getName());

    private final ServiceUrls serviceUrls;

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ResponseError> generalException(Exception ex) {
        logger.info(() -> "Unhandled exception caught: " + getExceptionCause(ex));
        logger.info(() -> "Exception name: " + ex.getClass().getName());

        return internalError();
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ResponseError> missingServletRequestParameterException(MissingServletRequestParameterException ex) {
        return ResponseEntity.badRequest().body(
                new ResponseError(ex.getMessage())
        );
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ResponseError> methodArgumentTypeMismatchException(MethodArgumentTypeMismatchException ex) {
        return ResponseEntity.badRequest().body(
                new ResponseError(ex.getMessage())
        );
    }

    @ExceptionHandler(ServiceException.class)
    public ResponseEntity<ResponseError> serviceExceptionHandler(ServiceException ex) {
        if (ex.getErrorCategory() == ErrorCategory.INTERNAL_ERROR)
            return internalError();

        return ResponseEntity.status(ex.getErrorCategory().getHttpStatusCode()).body(
                new ResponseError(ex.getMessage())
        );
    }

    @ExceptionHandler(JpaSystemException.class)
    public ResponseEntity<ResponseError> jpaSystemException(JpaSystemException ex) {
        return ResponseEntity.badRequest().body(new ResponseError(
                extractRelevantErrorMessage(ex.getMessage())
        ));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ResponseError> illegalArgumentException(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(new ResponseError(ex.getMessage()));
    }

    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<ResponseError> emailAlreadyExistsException(EmailAlreadyExistsException ex) {
        return ResponseEntity.badRequest().body(
                new ResponseError("This email is already in use")
        );
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ResponseError> badCredentialsException(BadCredentialsException ex) {
        return ResponseEntity.badRequest().body(new ResponseError(ex.getMessage()));
    }

    @ExceptionHandler(WithoutPermissionException.class)
    public ResponseEntity<ResponseError> withoutPermissionException(WithoutPermissionException ex) {
        return ResponseEntity.status(ErrorCategory.FORBIDDEN.getHttpStatusCode()).body(
                new ResponseError("Whitout permission to perform this action")
        );
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ResponseError> notFoundException(NotFoundException ex) {
        return ResponseEntity.badRequest().body(
                new ResponseError("Entity not found")
        );
    }

    @ExceptionHandler(EmailSendingException.class)
    public ResponseEntity<ResponseError> emailSendingException(EmailSendingException ex) {
        return internalError();
    }

    @ExceptionHandler(InvalidHashAlgorithmException.class)
    public ResponseEntity<ResponseError> invalidHashAlgorithmException(InvalidHashAlgorithmException ex) {
        return internalError();
    }

    @ExceptionHandler(TokenCreationException.class)
    public ResponseEntity<ResponseError> tokenCreationException(TokenCreationException ex) {
        return internalError();
    }

    @ExceptionHandler(EmptyFileException.class)
    public ResponseEntity<ResponseError> emptyFileException(EmptyFileException ex) {
        return ResponseEntity.badRequest().body(
                new ResponseError("The file is empty")
        );
    }

    @ExceptionHandler(InvalidFileException.class)
    public ResponseEntity<ResponseError> invalidFileException(InvalidFileException ex) {
        return ResponseEntity.badRequest().body(
                new ResponseError(ex.getMessage())
        );
    }

    @ExceptionHandler(FileCompressionErrorException.class)
    public ResponseEntity<ResponseError> compressionErroException(FileCompressionErrorException ex) {
        return internalError();
    }

    @ExceptionHandler(CannotChangePasswordException.class)
    public ResponseEntity<ResponseError> cannotChangePasswordException(CannotChangePasswordException ex) {
        return ResponseEntity.badRequest().body(new ResponseError(ex.getMessage()));
    }

    @ExceptionHandler(InvalidParametersException.class)
    public ResponseEntity<ResponseError> invalidParametersException(InvalidParametersException ex) {
        return ResponseEntity.badRequest().body(new ResponseError(ex.getMessage()));
    }

    @ExceptionHandler(FileIOException.class)
    public ResponseEntity<ResponseError> fileIOException(FileIOException ex) {
        return internalError();
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ResponseError> httpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException ex) {
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(
                new ResponseError("Method not allowed")
        );
    }

    @ExceptionHandler(ExpiredLinkException.class)
    public ResponseEntity<ResponseError> expiredLinkException(ExpiredLinkException ex) {
        final URI uri = URI.create(serviceUrls.getSiteUrl() + "/link-expirado");

        return ResponseEntity.status(HttpStatus.SEE_OTHER)
                .location(uri)
                .build();
    }

    private ResponseEntity<ResponseError> internalError() {
        return ResponseEntity.internalServerError().body(
                new ResponseError(INTERNAL_ERROR)
        );
    }

    private String getExceptionCause(Exception ex) {
        if (ex.getMessage() != null)
            return ex.getMessage();

        if (ex.getCause() != null)
            return ex.getCause().getMessage();

        return ex.getLocalizedMessage();
    }
}
