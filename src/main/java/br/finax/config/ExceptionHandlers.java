package br.finax.config;

import br.finax.dto.ResponseError;
import br.finax.exceptions.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ExceptionHandlers {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ResponseError> generalExceptionHandler(Exception ex) {
        return ResponseEntity.internalServerError().body(
                new ResponseError(ex.getMessage())
        );
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ResponseError> illegalArgumentException(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(new ResponseError(ex.getMessage()));
    }

    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<ResponseError> emailAlreadyExistsException(EmailAlreadyExistsException ex) {
        return ResponseEntity.badRequest().body(
                new ResponseError("this email is already in use")
        );
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ResponseError> unauthorizedException(UnauthorizedException ex) {
        return ResponseEntity.status(401).build();
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ResponseError> badCredentialsException(BadCredentialsException ex) {
        return ResponseEntity.badRequest().body(new ResponseError(ex.getMessage()));
    }

    @ExceptionHandler(WithoutPermissionException.class)
    public ResponseEntity<ResponseError> withoutPermissionException(WithoutPermissionException ex) {
        return ResponseEntity.badRequest().body(
                new ResponseError("whitout permission to perform this action")
        );
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ResponseError> notFoundException(NotFoundException ex) {
        return ResponseEntity.badRequest().body(
                new ResponseError("entity not found")
        );
    }

    @ExceptionHandler(UnsendedEmailException.class)
    public ResponseEntity<ResponseError> unsendedEmailException(UnsendedEmailException ex) {
        return ResponseEntity.internalServerError().body(new ResponseError(ex.getMessage()));
    }

    @ExceptionHandler(InvalidHashAlgorithmException.class)
    public ResponseEntity<ResponseError> invalidHashAlgorithmException(InvalidHashAlgorithmException ex) {
        return ResponseEntity.internalServerError().body(new ResponseError(ex.getMessage()));
    }

    @ExceptionHandler(TokenCreationException.class)
    public ResponseEntity<ResponseError> tokenCreationException(TokenCreationException ex) {
        return ResponseEntity.internalServerError().body(new ResponseError(ex.getMessage()));
    }

    @ExceptionHandler(EmptyFileException.class)
    public ResponseEntity<ResponseError> emptyFileException(EmptyFileException ex) {
        return ResponseEntity.badRequest().body(
                new ResponseError("the file is empty")
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
        return ResponseEntity.internalServerError().body(new ResponseError(ex.getMessage()));
    }

    @ExceptionHandler(CannotChangePasswordException.class)
    public ResponseEntity<ResponseError> cannotChangePasswordException(CannotChangePasswordException ex) {
        return ResponseEntity.badRequest().body(new ResponseError(ex.getMessage()));
    }
}
