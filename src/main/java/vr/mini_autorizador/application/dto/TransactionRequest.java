package vr.mini_autorizador.application.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

public record TransactionRequest(
        @JsonProperty("numeroCartao")
        String cardNumber,
        @JsonProperty("senha")
        String cardPassword,
        @JsonProperty("valor")
        BigDecimal amount) {
}
