package vr.mini_autorizador.application.usecase;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import vr.mini_autorizador.application.dto.CardRequest;
import vr.mini_autorizador.application.dto.CardResponse;
import vr.mini_autorizador.domain.exception.CardDomainException;
import vr.mini_autorizador.domain.exception.CardNotFoundException;
import vr.mini_autorizador.domain.factory.CardFactory;
import vr.mini_autorizador.domain.model.Card;
import vr.mini_autorizador.domain.repository.CardRepository;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CardUseCaseTest {

    @Mock
    private CardRepository cardRepository;

    @Mock
    private CardFactory cardFactory;

    @InjectMocks
    private CardUseCase cardUseCase;

    private static final String VALID_CARD_NUMBER = "6549873025634501";
    private static final String VALID_PASSWORD = "1234";
    private static final BigDecimal INITIAL_BALANCE = new BigDecimal("500.00");

    private Card createTestCard(String cardNumber, String password, BigDecimal balance) {
        Card card = new Card();
        card.setId(1L);
        card.setCardNumber(cardNumber);
        card.setCardPassword(password);
        card.setBalance(balance);
        card.setVersion(0L);
        return card;
    }

    // ==================== Card Creation Tests ====================

    @Test
    @DisplayName("Deve criar cartão com sucesso com saldo inicial de R$500.00")
    void shouldCreateCardSuccessfullyWithInitialBalanceOf500() {
        CardRequest request = new CardRequest(VALID_CARD_NUMBER, VALID_PASSWORD);
        Card newCard = createTestCard(VALID_CARD_NUMBER, VALID_PASSWORD, INITIAL_BALANCE);
        
        when(cardRepository.findCardByCardNumber(VALID_CARD_NUMBER)).thenReturn(Optional.empty());
        when(cardFactory.createNewCard(VALID_CARD_NUMBER, VALID_PASSWORD)).thenReturn(newCard);
        when(cardRepository.save(any(Card.class))).thenReturn(newCard);

        CardResponse response = cardUseCase.create(request);

        assertNotNull(response);
        assertEquals(VALID_CARD_NUMBER, response.cardNumber());
        assertEquals(VALID_PASSWORD, response.cardPassword());
        
        verify(cardRepository, times(1)).findCardByCardNumber(VALID_CARD_NUMBER);
        verify(cardFactory, times(1)).createNewCard(VALID_CARD_NUMBER, VALID_PASSWORD);
        verify(cardRepository, times(1)).save(newCard);
    }

    @Test
    @DisplayName("Deve lançar CardDomainException quando número do cartão já existe")
    void shouldThrowCardDomainExceptionWhenCardNumberAlreadyExists() {
        CardRequest request = new CardRequest(VALID_CARD_NUMBER, VALID_PASSWORD);
        Card existingCard = createTestCard(VALID_CARD_NUMBER, "5678", INITIAL_BALANCE);
        
        when(cardRepository.findCardByCardNumber(VALID_CARD_NUMBER)).thenReturn(Optional.of(existingCard));

        CardDomainException exception = assertThrows(CardDomainException.class, () -> {
            cardUseCase.create(request);
        });

        assertNotNull(exception.getResponse());
        assertTrue(exception.getResponse() instanceof CardResponse);
        CardResponse responseInException = (CardResponse) exception.getResponse();
        assertEquals(VALID_CARD_NUMBER, responseInException.cardNumber());
        assertEquals("5678", responseInException.cardPassword());
        
        verify(cardRepository, times(1)).findCardByCardNumber(VALID_CARD_NUMBER);
        verify(cardFactory, never()).createNewCard(any(), any());
        verify(cardRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve usar CardFactory para criar cartão com parâmetros corretos")
    void shouldUseCardFactoryToCreateCardWithCorrectParameters() {
        CardRequest request = new CardRequest(VALID_CARD_NUMBER, VALID_PASSWORD);
        Card newCard = createTestCard(VALID_CARD_NUMBER, VALID_PASSWORD, INITIAL_BALANCE);
        
        when(cardRepository.findCardByCardNumber(VALID_CARD_NUMBER)).thenReturn(Optional.empty());
        when(cardFactory.createNewCard(VALID_CARD_NUMBER, VALID_PASSWORD)).thenReturn(newCard);
        when(cardRepository.save(any(Card.class))).thenReturn(newCard);

        cardUseCase.create(request);

        verify(cardFactory, times(1)).createNewCard(
            eq(VALID_CARD_NUMBER),
            eq(VALID_PASSWORD)
        );
        
        assertEquals(INITIAL_BALANCE, newCard.getBalance());
    }

    // ==================== Balance Query Tests ====================

    @Test
    @DisplayName("Deve retornar saldo correto para cartão existente")
    void shouldReturnCorrectBalanceForExistingCard() {
        BigDecimal expectedBalance = new BigDecimal("350.75");
        Card existingCard = createTestCard(VALID_CARD_NUMBER, VALID_PASSWORD, expectedBalance);
        
        when(cardRepository.findCardByCardNumber(VALID_CARD_NUMBER)).thenReturn(Optional.of(existingCard));

        BigDecimal actualBalance = cardUseCase.getBalance(VALID_CARD_NUMBER);

        assertNotNull(actualBalance);
        assertEquals(expectedBalance, actualBalance);
        assertEquals(0, expectedBalance.compareTo(actualBalance));
        
        verify(cardRepository, times(1)).findCardByCardNumber(VALID_CARD_NUMBER);
    }

    @Test
    @DisplayName("Deve lançar CardNotFoundException quando cartão não existe")
    void shouldThrowCardNotFoundExceptionWhenCardDoesNotExist() {
        String nonExistentCardNumber = "9999999999999999";
        when(cardRepository.findCardByCardNumber(nonExistentCardNumber)).thenReturn(Optional.empty());

        CardNotFoundException exception = assertThrows(CardNotFoundException.class, () -> {
            cardUseCase.getBalance(nonExistentCardNumber);
        });

        assertEquals("CARTAO_NAO_ENCONTRADO", exception.getMessage());
        
        verify(cardRepository, times(1)).findCardByCardNumber(nonExistentCardNumber);
    }

    @Test
    @DisplayName("Deve retornar saldo inicial de R$500.00 para cartão recém-criado")
    void shouldReturnInitialBalanceOf500ForNewlyCreatedCard() {
        Card newCard = createTestCard(VALID_CARD_NUMBER, VALID_PASSWORD, INITIAL_BALANCE);
        when(cardRepository.findCardByCardNumber(VALID_CARD_NUMBER)).thenReturn(Optional.of(newCard));

        BigDecimal balance = cardUseCase.getBalance(VALID_CARD_NUMBER);

        assertNotNull(balance);
        assertEquals(INITIAL_BALANCE, balance);
        assertEquals(new BigDecimal("500.00"), balance);
        
        verify(cardRepository, times(1)).findCardByCardNumber(VALID_CARD_NUMBER);
    }
}
