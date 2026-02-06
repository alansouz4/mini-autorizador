package vr.mini_autorizador.domain.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import vr.mini_autorizador.domain.exception.CardDomainException;
import vr.mini_autorizador.domain.model.Card;
import vr.mini_autorizador.domain.repository.CardRepository;
import vr.mini_autorizador.domain.rule.AuthorizationRule;
import vr.mini_autorizador.domain.rule.CardExistsRule;
import vr.mini_autorizador.domain.rule.CardPasswordRule;
import vr.mini_autorizador.domain.rule.SufficientBalanceRule;

import java.math.BigDecimal;

/**
 * Serviço de Domínio: AutorizacaoService
 *
 * Padrões aplicados:
 * - Strategy Pattern: Cada regra é uma estratégia de validação
 * - Chain of Responsibility: Regras são encadeadas e executadas em sequência
 * - Domain Service: Orquestra lógica que não pertence a uma entidade específica
 */
@Service
public class AuthorizationService {
    
    private final CardRepository cardRepository;
    private final AuthorizationRule authorizationChain;
    
    @Autowired
    public AuthorizationService(
            CardRepository cardRepository,
            CardExistsRule cardExistsRule,
            CardPasswordRule cardPasswordRule,
            SufficientBalanceRule sufficientBalanceRule) {
        
        this.cardRepository = cardRepository;

        // Configura Chain of Responsibility
        // Ordem: Existência -> Senha -> Saldo
        cardExistsRule.setNext(cardPasswordRule);
        cardPasswordRule.setNext(sufficientBalanceRule);
        
        this.authorizationChain = cardExistsRule;
    }

    public Card authorizer(String cardNumber, String password, BigDecimal amount) {
        Card card = cardRepository.findCardByCardNumber(cardNumber)
                .orElseThrow(() -> new CardDomainException("CARTAO_INEXISTENTE"));
        
        // Executa a cadeia de regras
        authorizationChain.validateAndNext(card, password, amount);
        
        return card;
    }

    public void processorTransaction(Card card, BigDecimal amount) {
        // Atualiza o saldo (domain model)
        BigDecimal newBalance = card.toDebitBalance(amount);
        card.setBalance(newBalance);

        cardRepository.save(card);
    }
}
