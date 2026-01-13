package vr.mini_autorizador.application.dto;

import tools.jackson.databind.annotation.JsonNaming;

import java.math.BigDecimal;

@JsonNaming
public record CardResponse(String cardPassword, String cardNumber) {
}
