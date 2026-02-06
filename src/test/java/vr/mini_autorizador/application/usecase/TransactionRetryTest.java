package vr.mini_autorizador.application.usecase;

import jakarta.persistence.OptimisticLockException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.dao.OptimisticLockingFailureException;
import vr.mini_autorizador.application.dto.TransactionRequest;
import vr.mini_autorizador.domain.model.Card;
import vr.mini_autorizador.domain.service.AuthorizationService;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@SpringBootTest
class TransactionRetryTest {

    @MockitoBean
    private AuthorizationService authorizationService;

    @Autowired
    private TransactionUseCase transactionUseCase;

    private static final String CARD_NUMBER = "1234567890123456";
    private static final String PASSWORD = "1234";
    private static final BigDecimal AMOUNT = new BigDecimal("10.00");

    @Test
    @DisplayName("Deve disparar retry quando OptimisticLockException ocorrer")
    void shouldRetryWhenOptimisticLockExceptionOccurs() {
        TransactionRequest request = new TransactionRequest(CARD_NUMBER, PASSWORD, AMOUNT);
        Card card = new Card();

        when(authorizationService.authorizer(anyString(), anyString(), any(BigDecimal.class)))
                .thenThrow(new OptimisticLockException("Lock conflict"))
                .thenReturn(card);

        transactionUseCase.process(request);

        verify(authorizationService, times(2)).authorizer(CARD_NUMBER, PASSWORD, AMOUNT);
        verify(authorizationService, times(1)).processorTransaction(card, AMOUNT);
    }

    @Test
    @DisplayName("Deve disparar retry quando OptimisticLockingFailureException ocorrer")
    void shouldRetryWhenOptimisticLockingFailureExceptionOccurs() {
        TransactionRequest request = new TransactionRequest(CARD_NUMBER, PASSWORD, AMOUNT);
        Card card = new Card();

        when(authorizationService.authorizer(anyString(), anyString(), any(BigDecimal.class)))
                .thenThrow(new OptimisticLockingFailureException("Spring Lock conflict"))
                .thenReturn(card);

        transactionUseCase.process(request);

        verify(authorizationService, times(2)).authorizer(CARD_NUMBER, PASSWORD, AMOUNT);
    }

    @Test
    @DisplayName("Deve exaurir tentativas e lançar exceção após falhas consecutivas")
    void shouldThrowExceptionWhenRetriesAreExhausted() {
        TransactionRequest request = new TransactionRequest(CARD_NUMBER, PASSWORD, AMOUNT);

        when(authorizationService.authorizer(anyString(), anyString(), any(BigDecimal.class)))
                .thenThrow(new OptimisticLockingFailureException("Persistent lock failure"));

        assertThrows(OptimisticLockingFailureException.class, () -> 
            transactionUseCase.process(request)
        );

        verify(authorizationService, times(5)).authorizer(CARD_NUMBER, PASSWORD, AMOUNT);
    }
}
