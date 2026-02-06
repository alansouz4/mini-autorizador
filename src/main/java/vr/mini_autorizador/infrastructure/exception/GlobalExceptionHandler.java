package vr.mini_autorizador.infrastructure.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import vr.mini_autorizador.domain.exception.CardDomainException;
import vr.mini_autorizador.domain.exception.CardNotFoundException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CardNotFoundException.class)
    public ResponseEntity<Void> handleCardNotFoundException(CardNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }

    @ExceptionHandler(CardDomainException.class)
    public ResponseEntity<Object> handleIllegalStateException(CardDomainException ex) {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(ex.getResponse());
    }
}
