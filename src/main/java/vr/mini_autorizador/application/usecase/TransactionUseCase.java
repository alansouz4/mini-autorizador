package vr.mini_autorizador.application.usecase;

import jakarta.persistence.OptimisticLockException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vr.mini_autorizador.application.dto.TransactionRequest;
import vr.mini_autorizador.domain.model.Card;
import vr.mini_autorizador.domain.service.AuthorizationService;

/**
 * Use Case: Processar Transação
 *
 * Responsabilidades:
 * - Orquestrar o fluxo de processamento de transação
 * - Delegar validações para o serviço de domínio (AuthorizationService)
 * - Manter a camada de aplicação fina e focada em orquestração
 * - Garantir ACID e concorrência segura com Optimistic Locking + Retry
 *
 * Padrões aplicados:
 * - @Transactional: Garante atomicidade (ACID)
 * - @Retryable: Retry automático em caso de conflito de versão
 * - Optimistic Locking: Campo @Version no Card previne race conditions
 */
@Service
public class TransactionUseCase {

    @Autowired
    private AuthorizationService authorizationService;

    /**
     * Processa uma transação com retry automático em caso de conflito
     *
     * @Transactional: Garante que toda a operação seja atômica
     * @Retryable: Tenta novamente se houver OptimisticLockException
     *   - maxAttempts = 3: Tenta até 3 vezes
     *   - backoff: Espera 100ms entre tentativas (evita contenção)
     *
     * Fluxo:
     * 1. Autoriza a transação (valida cartão, senha, saldo)
     * 2. Debita o valor (atualiza saldo com controle de versão)
     * 3. Se houver conflito de versão, retry automático
     *
     * @param request Dados da transação (número do cartão, senha, valor)
     * @throws vr.mini_autorizador.domain.exception.CardDomainException se autorização falhar
     * @throws OptimisticLockException se exceder tentativas de retry
     */
    @Transactional
    @Retryable(
            retryFor = OptimisticLockException.class,
            maxAttempts = 3,
            backoff = @Backoff(delay = 100, multiplier = 2)
    )
    public void process(TransactionRequest request) {
        // 1. Autoriza a transação (aplica todas as regras de negócio)
        Card authorizedCard = authorizationService.authorizer(
                request.cardNumber(),
                request.cardPassword(),
                request.amount()
        );
        
        // 2. Processa a transação autorizada (debita o valor)
        // Optimistic Locking: Se outra transação modificou o card, lança OptimisticLockException
        authorizationService.processorTransaction(authorizedCard, request.amount());
    }
}
