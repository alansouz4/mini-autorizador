package vr.mini_autorizador.domain.factory;

import org.springframework.stereotype.Component;
import vr.mini_autorizador.domain.model.Card;

import java.math.BigDecimal;

/**
 * Simple Factory Pattern: CardFactory
 * 
 * Responsabilidade:
 * - Centralizar a criação de objetos Card
 * - Garantir que todos os cartões sejam criados com saldo inicial de R$500,00
 * - Encapsular a lógica de inicialização complexa
 * 
 * Benefícios:
 * - Consistência: todos os cartões têm o mesmo saldo inicial
 * - Single Responsibility: separação entre criação e lógica de negócio
 * - Facilita testes: mock da factory em vez de construtor
 * - Facilita mudanças: alterar regra de saldo inicial em um único lugar
 */
@Component
public class CardFactory {
    
    private static final BigDecimal INITIAL_BALANCE = new BigDecimal("500.00");
    
    /**
     * Cria um novo cartão com saldo inicial padrão
     * 
     * @param cardNumber Número do cartão
     * @param cardPassword Senha do cartão
     * @return Card novo cartão inicializado com saldo de R$500,00
     */
    public Card createNewCard(String cardNumber, String cardPassword) {
        Card card = new Card();
        card.setCardNumber(cardNumber);
        card.setCardPassword(cardPassword);
        card.setBalance(INITIAL_BALANCE);
        
        return card;
    }
    
    /**
     * Retorna o saldo inicial padrão para novos cartões
     * Útil para testes e validações
     * 
     * @return BigDecimal saldo inicial (R$500,00)
     */
    public BigDecimal getInitialBalance() {
        return INITIAL_BALANCE;
    }
}
