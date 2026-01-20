package vr.mini_autorizador.domain.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import vr.mini_autorizador.domain.exception.CardDomainException;
import vr.mini_autorizador.domain.model.Card;
import vr.mini_autorizador.domain.repository.CardRepository;
import vr.mini_autorizador.domain.rule.CardExistsRule;
import vr.mini_autorizador.domain.rule.CardPasswordRule;
import vr.mini_autorizador.domain.rule.SufficientBalanceRule;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthorizationServiceTest {

    @Mock
    private CardRepository cardRepository;
    @Spy
    private CardExistsRule cardExistsRule;
    @Spy
    private CardPasswordRule cardPasswordRule;
    @Spy
    private SufficientBalanceRule sufficientBalanceRule;

    private AuthorizationService authorizationService;

    private static final String CARD_NUMBER = "6549873025634501";
    private static final String PASSWORD = "1234";
    private static final BigDecimal AMOUNT = new BigDecimal("10.00");
    private static final BigDecimal BALANCE = new BigDecimal("500.00");

    @BeforeEach
    void setUp() {
        authorizationService = new AuthorizationService(
                cardRepository,
                cardExistsRule,
                cardPasswordRule,
                sufficientBalanceRule
        );
    }

    @Test
    @DisplayName("Deve autorizar cartão quando todas as regras passam")
    void shouldAuthorizeCardWhenAllRulesPass() {
        Card card = new Card();
        card.setId(1L);
        card.setCardNumber(CARD_NUMBER);
        card.setCardPassword(PASSWORD);
        card.setBalance(BALANCE);

        when(cardRepository.findCardByCardNumber(CARD_NUMBER)).thenReturn(Optional.of(card));

        Card result = authorizationService.authorizer(CARD_NUMBER, PASSWORD, AMOUNT);

        assertEquals(card, result);
        verify(cardExistsRule, times(1)).validateAndNext(card, PASSWORD, AMOUNT);
    }

    @Test
    @DisplayName("Deve lançar exceção quando cartão não é encontrado no repositório")
    void shouldThrowExceptionWhenCardNotFound() {
        when(cardRepository.findCardByCardNumber(CARD_NUMBER)).thenReturn(Optional.empty());

        CardDomainException exception = assertThrows(CardDomainException.class, () ->
            authorizationService.authorizer(CARD_NUMBER, PASSWORD, AMOUNT)
        );

        assertEquals("CARTAO_INEXISTENTE", exception.getResponse());
        verify(cardExistsRule, never()).validateAndNext(any(), any(), any());
    }

    @Test
    @DisplayName("Deve atualizar saldo e salvar cartão no processamento")
    void shouldUpdateBalanceAndSaveCardOnProcessing() {
        Card card = new Card();
        card.setBalance(new BigDecimal("100.00"));

        authorizationService.processorTransaction(card, AMOUNT);

        assertEquals(new BigDecimal("90.00"), card.getBalance());
        verify(cardRepository, times(1)).save(card);
    }
}
