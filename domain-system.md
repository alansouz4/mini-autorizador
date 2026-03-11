## Domínios do Sistema – Mini Autorizador

Os domínios centrais são:
- Cartão (entidade principal)
- Saldo (valor monetário associado ao cartão)
- Transação (evento de pagamento)
- Autorizador (serviço que aplica regras)
- Usuário/Autenticação (controle de acesso)

1. ### Cartão
- Representa o meio de pagamento (Vale Refeição/Alimentação/Crédito Livre/Home office).
- **Atributos principais**:
  - Número do cartão
  - Senha
  - Saldo
- **Regras**:
  - Todo cartão é criado com saldo inicial de R$500,00.
  - Deve ser persistido no banco.
  - Não pode haver duplicidade de número de cartão.

2. ### Saldo
- Representa o valor monetário disponível para uso no cartão.
- **Responsabilidades**:
  - Consultar saldo atual.
  - Atualizar saldo após transações autorizadas.
  - Validar se há saldo suficiente para a operação.

3. ### Transação
- Representa uma tentativa de pagamento usando o cartão.
- **Atributos principais**:
  - Número do cartão
  - Senha informada
  - Valor da transação
- **Regras de autorização**:
  - O cartão deve existir.
  - A senha deve ser válida.
  - O saldo deve ser suficiente.
- **Resultado**:
  - Autorizada → saldo é debitado.
  - Negada → retorna motivo (CARTAO_INEXISTENTE, SENHA_INVALIDA, SALDO_INSUFICIENTE).

4. ### Autorizador
- Domínio responsável por aplicar as regras de autorização.
- **Responsabilidades**:
  - Receber requisição de transação.
  - Validar regras de negócio.
  - Decidir se a transação é aprovada ou negada.
  - Retornar resposta conforme contrato REST.

5. ### Usuário / Autenticação
- Representa quem consome os serviços da API.
- **Responsabilidades**:
  - Autenticação via BASIC (username/password).
  - Garantir que apenas usuários autorizados possam criar cartões, consultar saldo e realizar transações.
