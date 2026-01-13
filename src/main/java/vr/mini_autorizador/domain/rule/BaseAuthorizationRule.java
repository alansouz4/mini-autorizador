package vr.mini_autorizador.domain.rule;

import vr.mini_autorizador.domain.model.Card;

import java.math.BigDecimal;

/**
 * Classe base abstrata para regras de autorização
 * Implementa Chain of Responsibility
 */
public abstract class BaseAuthorizationRule implements AuthorizationRule {
    
    protected AuthorizationRule next;
    
    @Override
    public void setNext(AuthorizationRule next) {
        this.next = next;
    }
    
    @Override
    public void validateAndNext(Card card, String password, BigDecimal amount) {
        validate(card, password, amount);

        // Chain of Responsibility: passa para a próxima regra
        // Usa Optional para evitar if
        java.util.Optional.ofNullable(next)
                .ifPresent(rule -> rule.validateAndNext(card, password, amount));
    }
}
