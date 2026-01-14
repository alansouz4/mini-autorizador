package vr.mini_autorizador.domain.rule;

import org.springframework.stereotype.Component;
import vr.mini_autorizador.domain.model.Card;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Regra: Valida se o cartão existe
 * Primeira regra na cadeia de autorização
 */
@Component
public class CardExistsRule extends BaseAuthorizationRule {
    
    @Override
    public void validate(Card card, String password, BigDecimal amount) {
        Objects.requireNonNull(card, "CARTAO_INEXISTENTE");
    }
}
