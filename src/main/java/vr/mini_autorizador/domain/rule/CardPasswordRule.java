package vr.mini_autorizador.domain.rule;

import org.springframework.stereotype.Component;
import vr.mini_autorizador.domain.model.Card;

import java.math.BigDecimal;

/**
 * Regra: Valida se a senha do cartão está correta
 * Segunda regra na cadeia de autorização
 */
@Component
public class CardPasswordRule extends BaseAuthorizationRule {
    
    @Override
    public void validate(Card card, String password, BigDecimal amount) {
        card.validPassword(password);
    }
}
