package vr.mini_autorizador.application.usecase;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import vr.mini_autorizador.application.dto.TransactionRequest;
import vr.mini_autorizador.domain.exception.CardDomainException;
import vr.mini_autorizador.domain.model.Card;
import vr.mini_autorizador.domain.repository.CardRepository;

import java.math.BigDecimal;

@Service
public class TransactionUseCase {

    @Autowired
    private CardRepository cardRepository;

    public void process(TransactionRequest request) {
        var cardByCardNumber = cardRepository.findCardByCardNumber(request.cardNumber())
                .orElseThrow(() -> new CardDomainException("CARTAO_INEXISTENTE"));
        validBusinessRule(request, cardByCardNumber);
        BigDecimal newBalance = updateBalance(cardByCardNumber.getBalance(), request.amount());
        updateCard(cardByCardNumber, newBalance);
    }

    private void validBusinessRule(TransactionRequest request, Card card) {
        if (!card.getCardPassword().equals(request.cardPassword())) {
            throw new CardDomainException("SENHA_INVALIDA");
        }

        if (card.getBalance().compareTo(request.amount()) < 0) {
            throw new CardDomainException("SALDO_INSUFICIENTE");
        }
    }

    private BigDecimal updateBalance(BigDecimal balance, BigDecimal amount) {
        return balance.subtract(amount);
    }

    private void updateCard(Card card, BigDecimal newBalance) {
        card.setBalance(newBalance);
        cardRepository.save(card);
    }
}
