package vr.mini_autorizador.domain.rule;

import vr.mini_autorizador.domain.model.Card;

import java.math.BigDecimal;

/**
 * Strategy Pattern: Interface para regras de autorização
 */
public interface AuthorizationRule {

    /**
     * Valida a regra de autorização
     * @param card Cartão a ser validado
     * @param password Senha informada na transação
     * @param amount Valor da transação
     * @throws vr.mini_autorizador.domain.exception.CardDomainException se a regra falhar
     */
    void validate(Card card, String password, BigDecimal amount);

    /**
     * Define a próxima regra na cadeia
     */
    void setNext(AuthorizationRule next);

    /**
     * Executa a validação e passa para a próxima regra se houver
     */
    default void validateAndNext(Card card, String password, BigDecimal amount) {
        validate(card, password, amount);
    }
}
