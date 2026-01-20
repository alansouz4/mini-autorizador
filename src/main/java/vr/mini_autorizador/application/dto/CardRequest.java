package vr.mini_autorizador.application.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record CardRequest(
        @JsonProperty("numeroCartao")
        String cardNumber,
        @JsonProperty("senha")
        String cardPassword) {
}
