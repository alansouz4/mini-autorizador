package vr.mini_autorizador.application.usecase;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import vr.mini_autorizador.application.dto.CardRequest;
import vr.mini_autorizador.application.dto.TransactionRequest;
import vr.mini_autorizador.domain.model.Card;
import vr.mini_autorizador.domain.repository.CardRepository;

import java.math.BigDecimal;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class TransactionConcurrencyTest {

    @Autowired
    private TransactionUseCase transactionUseCase;

    @Autowired
    private CardUseCase cardUseCase;

    @Autowired
    private CardRepository cardRepository;

    private static final String CARD_NUMBER = "1234567890123456";
    private static final String PASSWORD = "1234";

    @BeforeEach
    void setup() {
        cardRepository.deleteAll();
        cardUseCase.create(new CardRequest(CARD_NUMBER, PASSWORD));
    }

    @Test
    @DisplayName("Deve lidar com múltiplas transações simultâneas garantindo a integridade do saldo através de retry")
    void shouldHandleConcurrentTransactionsCorrectly() throws InterruptedException {
        int numberOfThreads = 5;
        BigDecimal transactionAmount = new BigDecimal("10.00");
        ExecutorService executorService = Executors.newFixedThreadPool(numberOfThreads);
        CountDownLatch latch = new CountDownLatch(numberOfThreads);

        TransactionRequest request = new TransactionRequest(CARD_NUMBER, PASSWORD, transactionAmount);

        for (int i = 0; i < numberOfThreads; i++) {
            executorService.submit(() -> {
                try {
                    transactionUseCase.process(request);
                } catch (Exception e) {
                    System.err.println("Erro na transação: " + e.getMessage());
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(10, TimeUnit.SECONDS);
        executorService.shutdown();

        Card card = cardRepository.findCardByCardNumber(CARD_NUMBER).orElseThrow();
        // Saldo inicial 500.00 - (5 threads * 10.00) = 450.00
        // Usando compareTo ou stripTrailingZeros para evitar problemas de escala de BigDecimal
        assertEquals(0, new BigDecimal("450.00").compareTo(card.getBalance()), 
                "O saldo final deve ser 450.00 após 5 transações de 10.00");
    }
}
