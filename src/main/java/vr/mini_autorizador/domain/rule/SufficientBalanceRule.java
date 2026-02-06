package vr.mini_autorizador.domain.rule;

import org.springframework.stereotype.Component;
import vr.mini_autorizador.domain.model.Card;

import java.math.BigDecimal;

/**
 * Regra: Valida se o cartão possui saldo suficiente
 * Terceira regra na cadeia de autorização
 */
@Component
public class SufficientBalanceRule extends BaseAuthorizationRule {
    
    @Override
    public void validate(Card card, String password, BigDecimal amount) {
        // Delega validação para o próprio cartão (domain model)
        card.validBalance(amount);
    }
}
