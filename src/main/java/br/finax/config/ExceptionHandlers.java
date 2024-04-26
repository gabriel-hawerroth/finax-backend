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
        return ResponseEntity.badRequest().body(new ResponseError(ex.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ResponseError> illegalArgumentException(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(new ResponseError(ex.getMessage()));
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ResponseError> unauthorizedException(UnauthorizedException ex) {
        return ResponseEntity.status(401).build();
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ResponseError> badCredentialsException(BadCredentialsException ex) {
        return ResponseEntity.badRequest().body(new ResponseError(ex.getMessage()));
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ResponseError> notFoundException(NotFoundException ex) {
        return ResponseEntity.badRequest().body(new ResponseError(ex.getMessage()));
    }

    @ExceptionHandler(UnsendedEmailException.class)
    public ResponseEntity<ResponseError> unsendedEmailException(UnsendedEmailException ex) {
        return ResponseEntity.internalServerError().body(new ResponseError(ex.getMessage()));
    }

    @ExceptionHandler(CompressionErrorException.class)
    public ResponseEntity<ResponseError> compressionErroException(CompressionErrorException ex) {
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
        return ResponseEntity.badRequest().body(new ResponseError(ex.getMessage()));
    }

    @ExceptionHandler(CannotChangePasswordException.class)
    public ResponseEntity<ResponseError> cannotChangePasswordException(CannotChangePasswordException ex) {
        return ResponseEntity.badRequest().body(new ResponseError(ex.getMessage()));
    }
}
