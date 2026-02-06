package vr.mini_autorizador.application.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record CardResponse(
        @JsonProperty("cardPassword")
        String cardPassword,
        @JsonProperty("cardNumber")
        String cardNumber) {
}
