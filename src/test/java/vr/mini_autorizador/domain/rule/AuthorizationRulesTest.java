package vr.mini_autorizador.domain.rule;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import vr.mini_autorizador.domain.exception.CardDomainException;
import vr.mini_autorizador.domain.model.Card;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthorizationRulesTest {

    @Test
    @DisplayName("Deve validar senha com sucesso")
    void shouldValidatePasswordSuccessfully() {
        Card card = new Card();
        card.setCardPassword("1234");
        CardPasswordRule rule = new CardPasswordRule();

        assertDoesNotThrow(() -> rule.validate(card, "1234", BigDecimal.ZERO));
    }

    @Test
    @DisplayName("Deve lançar exceção para senha inválida")
    void shouldThrowExceptionForInvalidPassword() {
        Card card = new Card();
        card.setCardPassword("1234");
        CardPasswordRule rule = new CardPasswordRule();

        CardDomainException exception = assertThrows(CardDomainException.class, () -> 
            rule.validate(card, "wrong", BigDecimal.ZERO)
        );
        assertEquals("SENHA_INVALIDA", exception.getResponse());
    }

    @Test
    @DisplayName("Deve validar saldo com sucesso")
    void shouldValidateBalanceSuccessfully() {
        Card card = new Card();
        card.setBalance(new BigDecimal("100.00"));
        SufficientBalanceRule rule = new SufficientBalanceRule();

        assertDoesNotThrow(() -> rule.validate(card, "any", new BigDecimal("50.00")));
        assertDoesNotThrow(() -> rule.validate(card, "any", new BigDecimal("100.00")));
    }

    @Test
    @DisplayName("Deve lançar exceção para saldo insuficiente")
    void shouldThrowExceptionForInsufficientBalance() {
        Card card = new Card();
        card.setBalance(new BigDecimal("100.00"));
        SufficientBalanceRule rule = new SufficientBalanceRule();

        CardDomainException exception = assertThrows(CardDomainException.class, () -> 
            rule.validate(card, "any", new BigDecimal("100.01"))
        );
        assertEquals("SALDO_INSUFICIENTE", exception.getResponse());
    }

    @Test
    @DisplayName("Deve validar existência do cartão com sucesso")
    void shouldValidateCardExistenceSuccessfully() {
        Card card = new Card();
        CardExistsRule rule = new CardExistsRule();

        assertDoesNotThrow(() -> rule.validate(card, "any", BigDecimal.ZERO));
    }

    @Test
    @DisplayName("Deve chamar a próxima regra na cadeia de autorização")
    void shouldCallNextRuleInChain() {
        Card card = new Card();
        card.setCardPassword("1234");
        
        CardPasswordRule rule1 = new CardPasswordRule();
        AuthorizationRule rule2 = mock(vr.mini_autorizador.domain.rule.AuthorizationRule.class);
        rule1.setNext(rule2);

        rule1.validateAndNext(card, "1234", BigDecimal.ZERO);

        verify(rule2, times(1)).validateAndNext(card, "1234", BigDecimal.ZERO);
    }
}
