package vr.mini_autorizador.application.usecase;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import vr.mini_autorizador.application.dto.TransactionRequest;
import vr.mini_autorizador.domain.model.Card;
import vr.mini_autorizador.domain.service.AuthorizationService;

/**
 * Use Case: Processar Transação
 *
 * Responsabilidade:
 * - Orquestrar o fluxo de processamento de transação
 * - Delegar validações para o serviço de domínio (AutorizacaoService)
 * - Manter a camada de aplicação fina e focada em orquestração
 *
 * Nota: Toda a lógica de negócio foi movida para AutorizacaoService (Domain Service)
 */
@Service
public class TransactionUseCase {

    @Autowired
    private AuthorizationService authorizationService;

    /**
     * Processa uma transação
     *
     * @param request Dados da transação (número do cartão, senha, valor)
     * @throws vr.mini_autorizador.domain.exception.CardDomainException se autorização falhar
     */
    public void process(TransactionRequest request) {
        // 1. Autoriza a transação (aplica todas as regras de negócio)
        Card authorizedCard = authorizationService.authorizer(
                request.cardNumber(),
                request.cardPassword(),
                request.amount()
        );
        
        // 2. Processa a transação autorizada (debita o valor)
        authorizationService.processorTransaction(authorizedCard, request.amount());
    }
}
