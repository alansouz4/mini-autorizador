# Mini Autorizador - VR Benefícios

Este projeto é parte de um teste técnico para a VR Benefícios.  
O objetivo é construir um mini-autorizador em **Java + Spring Boot** que permita criar cartões, consultar saldo e autorizar transações de forma simples e segura.

---

## 📋 Requisitos
- **Java 21 + Spring Boot + Maven**
- **Banco:** MySQL.
- **Persistência:** Spring Data JPA.
- **Migrações:** Flyway/Liquibase.
- **Segurança:** Spring Security (Basic Auth).
- **Testes:** JUnit 5, Mockito, Testcontainers.

---

## 🚀 Funcionalidades
- **Criar cartão**
    - Saldo inicial: R$500,00
    - Retorna erro `422` se cartão já existir
- **Consultar saldo**
    - Retorna saldo atual do cartão
    - Erro `404` se cartão não existir
- **Autorizar transação**
    - Regras de autorização:
        - Cartão deve existir
        - Senha deve ser correta
        - Saldo suficiente
    - Atualiza saldo em caso de sucesso
    - Retorna erro `422` com motivo:
        - `SALDO_INSUFICIENTE`
        - `SENHA_INVALIDA`
        - `CARTAO_INEXISTENTE`

---

## 🔒 Contratos REST
### 1. Criar novo cartão
```bash
curl -X POST "http://localhost:8080/cartoes" \
  -H "Content-Type: application/json" \
  -u username:password \
  -d '{
    "numeroCartao": "6549873025634501",
    "senha": "1234"
  }'
```
**Responses**
- 201 Created
```json
{
  "senha": "1234",
  "numeroCartao": "6549873025634501"
}
```
- 422 Unprocessable Entity → cartão já existe
- 401 Unauthorized → erro de autenticação

### 2. Obter saldo do cartão
```bash
curl -X GET "http://localhost:8080/cartoes/6549873025634501" \
  -u username:password
```
**Responses**
- 200 OK
```json
{
  "senha": "1234",
  "numeroCartao": "6549873025634501"
}
```
- 404 Not Found → cartão não existe
- 401 Unauthorized → erro de autenticação

### 3. Realizar uma transação
```bash
curl -X POST "http://localhost:8080/transacoes" \
  -H "Content-Type: application/json" \
  -u username:password \
  -d '{
    "numeroCartao": "6549873025634501",
    "senhaCartao": "1234",
    "valor": 10.00
  }'
```
**Responses**
- 201 Created
```json
OK
```
- 422 Unprocessable Entity → regras de autorização não atendidas
    - Possíveis mensagens:
        - SALDO_INSUFICIENTE
        - SENHA_INVALIDA
        - CARTAO_INEXISTENTE
- 401 Unauthorized → erro de autenticação

---

## 🏗️ Arquitetura e padrões
- Hexagonal Architecture (Ports & Adapters)
- DDD tático
- Entidade: Cartao
- Serviço de domínio: AutorizacaoService
- Design Patterns:
  - Repository
  - Strategy / Chain of Responsibility (regras de autorização sem if)
  - Factory (criação de cartões com saldo inicial)
  - Optimistic Locking (concorrência segura)

## ⚙️ Concorrência
Para evitar problemas em transações simultâneas:
- Lock otimista com versão do agregado
- Retry em caso de conflito
- Garantia de que saldo nunca ficará negativo

## 🗄️ Banco de dados
- SQL: MySQL
- ACID, atomicidade no débito de saldo

## 🧪 Testes
- Unitários: regras de negócio e invariantes
- Integração: endpoints REST e persistência
- Concorrência: simulação de transações simultâneas
- Cobertura: alta cobertura, testes validando comportamento real

📦 Como rodar
# Clone o repositório
```bash
git clone https://github.com/seu-usuario/mini-autorizador.git
cd mini-autorizador
```
# Suba os bancos com Docker Compose
```bash
docker-compose up -d
```

# Rode a aplicação
```bash
mvn spring-boot:run
```

> Acesse os endpoints em: ``` http://localhost:8080 ```

## 🔮 Próximos passos (opcionais)
- Implementar idempotência em transações
- Adicionar auditoria e logs estruturados
- Observabilidade com métricas e tracing
- Testes de carga com K6/Gatling

## 👨‍💻 Autor
Desenvolvido por Alan como parte do processo seletivo da VR Benefícios. 