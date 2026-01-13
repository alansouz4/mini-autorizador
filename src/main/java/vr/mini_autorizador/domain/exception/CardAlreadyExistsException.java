package vr.mini_autorizador.domain.exception;

import lombok.Getter;
import vr.mini_autorizador.application.dto.CardResponse;

@Getter
public class CardAlreadyExistsException extends RuntimeException {

    private final CardResponse response;

    public CardAlreadyExistsException(CardResponse response) {
        this.response = response;
    }
}
