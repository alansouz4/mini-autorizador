package vr.mini_autorizador.domain.factory;

import org.springframework.stereotype.Component;
import vr.mini_autorizador.domain.model.Card;

import java.math.BigDecimal;

/**
 * Factory
 *
 * - Centraliza a criação de objetos Card
 * - Garante que todos os cartões sejam criados com saldo inicial de R$500,00
 *
 */
@Component
public class CardFactory {
    
    private static final BigDecimal INITIAL_BALANCE = new BigDecimal("500.00");

    public Card createNewCard(String cardNumber, String cardPassword) {
        Card card = new Card();
        card.setCardNumber(cardNumber);
        card.setCardPassword(cardPassword);
        card.setBalance(INITIAL_BALANCE);
        
        return card;
    }
}
