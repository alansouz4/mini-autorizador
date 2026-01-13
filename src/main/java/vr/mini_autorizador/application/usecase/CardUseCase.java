package vr.mini_autorizador.application.usecase;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import vr.mini_autorizador.application.dto.CardRequest;
import vr.mini_autorizador.application.dto.CardResponse;
import vr.mini_autorizador.domain.exception.CardAlreadyExistsException;
import vr.mini_autorizador.domain.exception.CardNotFoundException;
import vr.mini_autorizador.domain.model.Card;
import vr.mini_autorizador.domain.repository.CardRepository;

import java.math.BigDecimal;

@Service
public class CardUseCase {

    private static final BigDecimal INITIAL_BALANCE = new BigDecimal("500.00");


    @Autowired
    private CardRepository cardRepository;

    public CardResponse create(CardRequest request) {
        cardRepository.findCardByCardNumber(request.cardNumber())
                .ifPresent(c -> {
                    throw new CardAlreadyExistsException(new CardResponse(c.getCardPassword(), c.getCardNumber()));
                });

        Card newCard = new Card();
        newCard.setCardNumber(request.cardNumber());
        newCard.setCardPassword(request.cardPassword());
        newCard.setBalance(INITIAL_BALANCE);

        Card save = cardRepository.save(newCard);
        return new CardResponse(save.getCardPassword(), save.getCardNumber());
    }

    public BigDecimal getBalance(String numeroCartao) {
        Card card = cardRepository.findCardByCardNumber(numeroCartao)
                .orElseThrow(() -> new CardNotFoundException("CARTAO_NAO_ENCONTRADO"));
        return card.getBalance();
    }
}
