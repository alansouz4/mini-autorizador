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
 * Use Case: Gestão de Cartões
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

    /**
     * Cria um novo cartão usando Simple Factory
     *
     * @param request Dados do cartão (número e senha)
     * @return CardResponse com dados do cartão criado
     * @throws CardDomainException se o cartão já existir
     */
    public CardResponse create(CardRequest request) {
        // valida se já existe cartão com o mesmo número
        cardRepository.findCardByCardNumber(request.cardNumber())
                .ifPresent(c -> {
                    throw new CardDomainException(new CardResponse(c.getCardPassword(), c.getCardNumber()));
                });

        // ussa Simple Factory para criar o cartão com saldo inicial consistente
        Card newCard = cardFactory.createNewCard(request.cardNumber(), request.cardPassword());

        Card savedCard = cardRepository.save(newCard);

        return new CardResponse(savedCard.getCardPassword(), savedCard.getCardNumber());
    }

    /**
     * Consulta o saldo de um cartão
     *
     * @param numeroCartao Número do cartão
     * @return BigDecimal saldo atual do cartão
     * @throws CardNotFoundException se o cartão não existir
     */
    public BigDecimal getBalance(String numeroCartao) {
        Card card = cardRepository.findCardByCardNumber(numeroCartao)
                .orElseThrow(() -> new CardNotFoundException("CARTAO_NAO_ENCONTRADO"));
        return card.getBalance();
    }
}