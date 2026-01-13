package vr.mini_autorizador.domain.exception;

import lombok.Getter;

@Getter
public class CardDomainException extends RuntimeException {

    private final Object response;

    public CardDomainException(Object response) {
        this.response = response;
    }
}
