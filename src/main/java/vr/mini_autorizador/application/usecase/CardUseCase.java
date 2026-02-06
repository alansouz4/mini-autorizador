package vr.mini_autorizador.application.usecase;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import vr.mini_autorizador.application.dto.CardRequest;
import vr.mini_autorizador.application.dto.CardResponse;
import vr.mini_autorizador.domain.exception.CardDomainException;
import vr.mini_autorizador.domain.exception.CardNotFoundException;
import vr.mini_autorizador.domain.factory.CardFactory;
import vr.mini_autorizador.domain.model.Card;
import vr.mini_autorizador.domain.repository.CardRepository;

import java.math.BigDecimal;

/**
 * Gestão de Cartões
 *
 * Responsabilidades:
 * - Orquestrar criação de cartões (delegando para CardFactory)
 * - Validar duplicidade de cartões
 * - Consultar saldo de cartões
 */
@Service
public class CardUseCase {

    @Autowired
    private CardRepository cardRepository;

    @Autowired
    private CardFactory cardFactory;

    public CardResponse create(CardRequest request) {
        // Valida se já existe cartão com o mesmo número
        cardRepository.findCardByCardNumber(request.cardNumber())
                .ifPresent(c -> {
                    throw new CardDomainException(new CardResponse(c.getCardPassword(), c.getCardNumber()));
                });

        // Usa Factory para criar o cartão com saldo inicial consistente
        Card newCard = cardFactory.createNewCard(request.cardNumber(), request.cardPassword());

        Card savedCard = cardRepository.save(newCard);

        return new CardResponse(savedCard.getCardPassword(), savedCard.getCardNumber());
    }

    public BigDecimal getBalance(String numeroCartao) {
        Card card = cardRepository.findCardByCardNumber(numeroCartao)
                .orElseThrow(() -> new CardNotFoundException("CARTAO_NAO_ENCONTRADO"));
        return card.getBalance();
    }
}