package vr.mini_autorizador.application.dto;

import java.math.BigDecimal;

public record TransactionRequest(String cardNumber, String cardPassword, BigDecimal amount) {
}
