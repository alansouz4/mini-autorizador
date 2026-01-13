package vr.mini_autorizador.domain.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import vr.mini_autorizador.domain.exception.CardDomainException;

import java.math.BigDecimal;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Card {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "card_number")
    private String cardNumber;

    @Column(name = "card_password")
    private String cardPassword;

    private BigDecimal balance;

    public BigDecimal toDebitBalance(BigDecimal amount) {
        return this.balance.subtract(amount);
    }

    public void validBalance(BigDecimal amount) {
        if (this.balance.compareTo(amount) < 0) {
            throw new CardDomainException("SALDO_INSUFICIENTE");
        }
    }

    public void validPassword(String cardPassword) {
        if(!this.cardPassword.equals(cardPassword)) {
            throw new CardDomainException("SENHA_INVALIDA");
        }
    }
}
