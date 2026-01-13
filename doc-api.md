# 📖 Documentação da API – Mini Autorizador

Este projeto implementa um **mini-autorizador** para cartões de benefícios (Vale Refeição/Alimentação).  
A aplicação foi desenvolvida em **Java + Spring Boot**, com interface totalmente **REST** e autenticação **BASIC**.

---

## 🔑 Autenticação
Todos os endpoints exigem autenticação **BASIC**:
- **Username:** `username`
- **Password:** `password`

---

## 📌 Endpoints

### 1. Criar novo cartão
```bash
  curl -X 'POST' \
  'http://localhost:8080/v1/cards' \
  -H 'accept: application/json' \
  -H 'Content-Type: application/json' \
  -u 'admin':'admin123' \
  -d '{
    "cardNumber": "6549873025634501",
    "cardPassword": "1234"
  }'
```
**Responses**
- 201 Created
```json
{
  "cardPassword": "1234",
  "cardNumber": "6549873025634501"
}
```
- 422 Unprocessable Entity → cartão já existe
- 401 Unauthorized → erro de autenticação

### 2. Obter saldo do cartão
```bash
curl -X GET "http://localhost:8080/v1/cards/6549873025634501" \
  -H 'accept: application/json' \
  -H 'Content-Type: application/json' \
  -u 'admin':'admin123'
```
**Responses**
- 200 OK
```json
{
  "balance": 500.0
}
```
- 404 Not Found → cartão não existe
- 401 Unauthorized → erro de autenticação

### 3. Realizar uma transação
```bash
curl -X POST "http://localhost:8080/transactions" \
  -H "Content-Type: application/json" \
  -u username:password \
  -d '{
    "cardNumber": "6549873025634501",
    "cardPassword": "1234",
    "balance": 10.00
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

## ⚙️ Regras de Autorização
Uma transação só será autorizada se:
- O cartão existir.
- A senha informada for correta.
- O cartão possuir saldo suficiente.

Caso qualquer regra falhe, a transação será negada com o respectivo motivo.
