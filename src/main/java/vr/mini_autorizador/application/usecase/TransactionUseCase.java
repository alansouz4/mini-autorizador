package vr.mini_autorizador.application.usecase;

import jakarta.persistence.OptimisticLockException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vr.mini_autorizador.application.dto.TransactionRequest;
import vr.mini_autorizador.domain.model.Card;
import vr.mini_autorizador.domain.service.AuthorizationService;

@Service
public class TransactionUseCase {

    @Autowired
    private AuthorizationService authorizationService;

    @Transactional
    @Retryable(
            retryFor = {OptimisticLockException.class, OptimisticLockingFailureException.class},
            maxAttempts = 5,
            backoff = @Backoff(delay = 100, multiplier = 2)
    )
    public void process(TransactionRequest request) {
        Card authorizedCard = authorizationService.authorizer(
                request.cardNumber(),
                request.cardPassword(),
                request.amount()
        );
        authorizationService.processorTransaction(authorizedCard, request.amount());
    }
}
