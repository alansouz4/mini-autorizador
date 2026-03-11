## Backlog – Mini Autorizador (VR Benefícios)
### Épico 1: Configuração do Projeto
- **História 1.1**: Como desenvolvedor, quero configurar o projeto em **Java + Maven + Spring Boot**, para garantir que a aplicação siga o padrão solicitado.
  - ✅ Tarefa: Criar projeto base com Spring Initializr. 
  - ✅ Tarefa: Configurar dependências no pom.xml (Spring Web, Spring Data, Lombok, etc.).
  - ✅ Tarefa: Configurar autenticação básica (username/password).
  - ✅ Tarefa: Criar estrutura inicial de pacotes (application, domain, infrastructure).
    - **Story Point**: 5
- **História 1.2**: Como desenvolvedor, quero configurar o docker-compose com banco relacional ou não relacional, para persistir os cartões.
  - ✅ Tarefa: Validar conexão com banco relacional.
  - ✅ Tarefa: Documentar no README a escolha do banco e suposições.
  - ✅ Tarefa: Comentar o banco não utilizado no docker-compose.yml.
    - **Story Point**: 3

### Épico 2: Gestão de Cartões
- **História 2.1**: Como usuário, quero criar um novo cartão com saldo inicial de R$500,00, para poder utilizá-lo em transações.
  - ✅ Tarefa: Implementar endpoint POST /cartoes.
  - ✅ Tarefa: Validar duplicidade de número de cartão (retornar 422).
  - ✅ Tarefa: Persistir cartão no banco.
  - ✅ Tarefa: Retornar resposta conforme contrato.
    - **Story Point**: 8
- **História 2.2**: Como usuário, quero consultar o saldo de um cartão existente, para acompanhar meus gastos.
  - ✅ Tarefa: Implementar endpoint GET /cartoes/{numeroCartao}.
  - ✅ Tarefa: Validar existência do cartão (404 se não encontrado).
  - ✅ Tarefa: Retornar saldo atual.
    - **Story Point**: 5
- **História 2.3**: Ajuste necessário para atender o contrato da API.
  - ✅ Fix: Ajustar path endpoints.

### Épico 3: Autorização de Transações
- **História 3.1**: Como usuário, quero realizar uma transação com meu cartão, para efetuar pagamentos.
  - ✅ Tarefa: Implementar endpoint POST /transacoes.
  - ✅ Tarefa: Validar existência do cartão (422 CARTAO_INEXISTENTE).
  - ✅ Tarefa: Validar senha do cartão (422 SENHA_INVALIDA).
  - ✅ Tarefa: Validar saldo disponível (422 SALDO_INSUFICIENTE).
  - ✅ Tarefa: Atualizar saldo do cartão se autorizado.
  - ✅ Tarefa: Retornar resposta conforme contrato.
  - ✅ Tarefa: Retornar resposta conforme contrato.
    - **Story Point**: 13
- **Tarefa 3.2**: Ajustar testes da classe AuthorizationService para validar regras de domínio.
    - ✅ Test: Ajustar e validar testes.

### Épico 4: Testes Automatizados
- **História 4.1**: Como desenvolvedor, quero implementar testes unitários e de integração, para garantir qualidade e cobertura.
  - ✅ Tarefa: Criar testes para criação de cartão.
  - ✅ Tarefa: Criar testes para consulta de saldo.
  - ✅ Tarefa: Criar testes para transações válidas e inválidas.
  - ✅ Tarefa: Garantir alta cobertura de código.
  - ✅ Tarefa: Configurar relatório de cobertura (Jacoco ou similar).
    - **Story Point**: 8

### Épico 5: Documentação
- **História 5.1**: Como avaliador, quero ler a documentação no README, para entender suposições e decisões de projeto.
  - ✅ Tarefa: Documentar suposições (ex.: escolha do banco, autenticação).
  - ✅ Tarefa: Documentar design patterns e boas práticas utilizadas.
  - ✅ Tarefa: Documentar instruções de execução (docker-compose + aplicação).
  - ✅ Tarefa: Documentar endpoints e contratos.
    - **Story Point**: 5

### Épico 6: Desafios Extras (Opcional)
- ✅ **História 6.1**: Como desenvolvedor, quero implementar a solução sem utilizar if, para explorar conceitos avançados de orientação a objetos.
  - **Story Point**: 8
- ✅ **História 6.2**: Como desenvolvedor, quero garantir concorrência segura em transações simultâneas, para evitar inconsistências de saldo.
  - Tarefa: Implementar controle de concorrência (ex.: locks, transações ACID).
  - Tarefa: Testar cenários de corrida com transações simultâneas.
    - **Story Point**: 13

  **Total Story Point**:
- Sem extras: 47
- Com extras: 68

---------------------------------------------------

## Observações
- Os pontos foram estimados utilizando a tabela de Story Points baseada em Fibonacci.
- Não é tempo em horas, mas esforço comparativo.
- Cada ponto representa um nível relativo de esforço, risco e incerteza.

### Tabela de Complexidade por Story Points

| Story Point | Nível de Complexidade | Justificativa |
|-------------|-----------------------|---------------|
| **1**       | Muito Baixa           | Tarefa trivial, sem dependências, rápida de implementar. |
| **2**       | Baixa                 | Simples, exige pouca lógica ou configuração. Poucas dependências. |
| **3**       | Moderada              | Alguma lógica de negócio, envolve persistência ou validações básicas. |
| **5**       | Média-Alta            | Requer múltiplas camadas (controller, service, repository) e testes. |
| **8**       | Alta                  | Envolve regras críticas, integrações ou cenários de negócio mais complexos. |
| **13**      | Muito Alta            | Complexidade elevada, riscos técnicos, concorrência ou performance. |
| **21**      | Extremamente Alta     | Requer pesquisa, spikes técnicos ou mudanças arquiteturais significativas. |