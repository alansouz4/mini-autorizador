package vr.mini_autorizador.application.usecase;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import vr.mini_autorizador.application.dto.TransactionRequest;
import vr.mini_autorizador.domain.exception.CardDomainException;
import vr.mini_autorizador.domain.model.Card;
import vr.mini_autorizador.domain.service.AuthorizationService;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionUseCaseTest {

    @Mock
    private AuthorizationService authorizationService;

    @InjectMocks
    private TransactionUseCase transactionUseCase;

    private static final String CARD_NUMBER = "6549873025634501";
    private static final String PASSWORD = "1234";
    private static final BigDecimal TRANSACTION_AMOUNT = new BigDecimal("10.00");

    private Card createTestCard() {
        Card card = new Card();
        card.setCardNumber(CARD_NUMBER);
        card.setCardPassword(PASSWORD);
        card.setBalance(new BigDecimal("500.00"));
        card.setVersion(0L);
        return card;
    }

    @Test
    @DisplayName("Deve autorizar e processar transação com sucesso")
    void shouldAuthorizeAndProcessTransactionSuccessfully() {
        TransactionRequest request = new TransactionRequest(CARD_NUMBER, PASSWORD, TRANSACTION_AMOUNT);
        Card card = createTestCard();

        when(authorizationService.authorizer(CARD_NUMBER, PASSWORD, TRANSACTION_AMOUNT))
                .thenReturn(card);

        assertDoesNotThrow(() -> transactionUseCase.process(request));

        verify(authorizationService, times(1)).authorizer(CARD_NUMBER, PASSWORD, TRANSACTION_AMOUNT);
        verify(authorizationService, times(1)).processorTransaction(card, TRANSACTION_AMOUNT);
    }

    @Test
    @DisplayName("Deve interromper processamento se autorização falhar")
    void shouldStopProcessingIfAuthorizationFails() {
        TransactionRequest request = new TransactionRequest(CARD_NUMBER, PASSWORD, TRANSACTION_AMOUNT);

        when(authorizationService.authorizer(anyString(), anyString(), any(BigDecimal.class)))
                .thenThrow(new CardDomainException("SALDO_INSUFICIENTE"));

        CardDomainException exception = assertThrows(CardDomainException.class, () -> 
            transactionUseCase.process(request)
        );

        assertEquals("SALDO_INSUFICIENTE", exception.getResponse());
        verify(authorizationService, times(1)).authorizer(anyString(), anyString(), any(BigDecimal.class));
        verify(authorizationService, never()).processorTransaction(any(), any());
    }
}
