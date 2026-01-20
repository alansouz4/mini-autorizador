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
  curl -X 'POST' \
  'http://localhost:8080/cartoes' \
  -H 'accept: application/json' \
  -H 'Content-Type: application/json' \
  -u 'admin':'admin123' \
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
  -H 'accept: application/json' \
  -H 'Content-Type: application/json' \
  -u 'admin':'admin123'
```
**Responses**
- 200 OK
```
500.0
```
- 404 Not Found → cartão não existe
- 401 Unauthorized → erro de autenticação

### 3. Realizar uma transação
```bash
curl -X POST "http://localhost:8080/transacoes" \
  -H "Content-Type: application/json" \
  -u 'admin':'admin123' \
  -d '{
    "numeroCartao": "6549873025634501",
    "senha": "1234",
    "valor": 100.00
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
## 🔐 Autenticação com Basic Auth (Spring Boot 4 / Spring Security 6)
O projeto mini-autorizador utiliza Basic Authentication para proteger suas rotas.
Esse mecanismo é simples e baseado em enviar as credenciais (usuário e senha) no cabeçalho da requisição HTTP.
A configuração é feita através de um bean SecurityFilterChain, que define quais endpoints exigem autenticação e quais são públicos.


### 📌 Como funciona
- O cliente envia o cabeçalho Authorization com o valor Basic <token>.
- O <token> é a string username:password codificada em Base64.
- O servidor valida as credenciais e, se corretas, permite o acesso ao recurso

### ⚙️ Classe de Configuração
```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable()) // desabilita CSRF para formulários HTML
            .authorizeHttpRequests(auth -> auth // autoriza todas as requisições
                .anyRequest().authenticated()
            )
            .httpBasic(); // habilita Basic Auth

        return http.build();
    }

    @Bean
    public InMemoryUserDetailsManager userDetailsService() {
        UserDetails admin = User.withUsername("admin")
            .password("{noop}123")
            .roles("USER")
            .build();

        return new InMemoryUserDetailsManager(admin); // cria um usuário em memória
    }
}
```

### 📂 Estrutura de segurança
```
/src
  /domain
  /application
  /infrastructure
    /auth
      SecurityConfig.java       // configuração técnica
```

---

## 🏗️ Arquitetura e padrões
- ### Hexagonal Architecture (Ports & Adapters)
 >A ideia da Arquitetura Hexagonal é separar o núcleo de negócio (domínio) das interfaces externas (web, banco, segurança). Isso facilita testes, manutenção e evolução.

#### 📂 Estrutura de pastas
```
src/main/java/com/vrbeneficios/miniautorizador
│
├── application        # Camada de aplicação (casos de uso)
│   ├── service        # Serviços que orquestram regras de negócio
│   └── dto            # DTOs para entrada/saída (REST)
│
├── domain             # Núcleo de negócio (entidades e regras)
│   ├── model          # Entidades (ex.: Cartao)
│   ├── repository     # Interfaces de repositórios (ports)
│   └── rule           # Regras de autorização (Strategy/Chain)
│
├── infrastructure     # Adapters (implementações técnicas)
│   ├── persistence    # Implementações de repositórios (JPA/MySQL)
│   ├── config         # Configurações (Spring, segurança, DB)
│   └── web            # Controllers REST (adapters de entrada)
│
└── MiniAutorizadorApplication.java  # Classe principal Spring Boot
```
#### 🔑 Fluxo
```
  - domain → contém a lógica pura (ex.: Cartao, AutorizacaoService, regras).
  - application → orquestra casos de uso (ex.: criar cartão, autorizar transação).
  - infrastructure → conecta com mundo externo (REST, DB, segurança).
```

- ### DDD tático
  - O DDD tático foca em como modelar o domínio com Entidades, Value Objects, Serviços de Domínio, Repositórios e Agregados.
```
src/main/java/com/vrbeneficios/miniautorizador/domain
│
├── model
│   └── Cartao.java              # Entidade principal
│
├── service
│   └── AutorizacaoService.java  # Serviço de domínio
│
├── repository
│   └── CartaoRepository.java    # Interface (Porta)
│
└── rule
    ├── AutorizacaoRule.java     # Interface Strategy
    ├── SenhaValidaRule.java     # Implementação
    ├── SaldoSuficienteRule.java # Implementação
    └── CartaoExistenteRule.java # Implementação
```
- ### Entidade: Cartao
  - A Entidade representa o núcleo do negócio.
    - Tem identidade (numeroCartao) única.
    - Possui atributos relevantes: senha, saldo, versão (para concorrência).
    - Contém invariantes: saldo ≥ 0, senha não nula.
    - Expõe comportamentos: debitar(valor), validarSenha(senha).
    - Exemplo com record:
```java
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Card {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "card_number")
    private String cardNumber;

    @Column(name = "card_password")
    private String cardPassword;

    private BigDecimal balance;

    @Version
    private Long version;

    public BigDecimal toDebitBalance(BigDecimal amount) {
        return this.balance.subtract(amount);
    }

    public void validBalance(BigDecimal amount) {
        if (this.balance.compareTo(amount) < 0) {
            throw new CardDomainException("SALDO_INSUFICIENTE");
        }
    }

    public void validPassword(String cardPassword) {
        if(!this.cardPassword.equals(cardPassword)) {
            throw new CardDomainException("SENHA_INVALIDA");
        }
    }
}
```
- ### Serviço de domínio: AutorizacaoService
  - O Serviço de Domínio orquestra regras que não pertencem a uma única entidade.
    - Aplica as regras de autorização (existência, senha, saldo).
    - Usa Strategy + Chain of Responsibility para evitar ifs.
    - Interage com o CartaoRepository (Porta) para buscar e atualizar cartões.

- ### Design Patterns:
  - Strategy 
    - O que é: encapsula algoritmos diferentes em classes distintas.
    - Uso: cada regra de autorização (SenhaValidaRule, SaldoSuficienteRule) é uma Strategy.
    - Benefício: evita if e facilita adicionar novas regras
    - Saber mais: https://refactoring.guru/pt-br/design-patterns/strategy

  - Chain of Responsibility (regras de autorização sem if)
    - O que é: permite encadear regras/handlers até que uma falhe ou todas passem.
    - Uso: sequência de validações de transação (existência, senha, saldo).
    - Benefício: desacopla regras e permite composição flexível
    - Saber mais: https://refactoring.guru/pt-br/design-patterns/chain-of-responsibility

  - Factory (criação de cartões com saldo inicial)
    - O que é: centraliza a criação de objetos complexos.
    - Uso: criação de Cartao sempre com saldo inicial de R$500,00.
    - Benefício: garante consistência na inicialização
    - Saber mais: https://refactoring.guru/pt-br/design-patterns/factory-method


## ⚙️ Concorrência
Para evitar problemas em transações simultâneas:

- ### Lock otimista com versão do agregado
  - O que é: usa versão do registro para evitar concorrência.
  - Uso: campo @Version em Cartao para garantir que duas transações não debitem além do saldo.
  - Benefício: segurança em cenários concorrentes sem travar o banco.
  - Saber mais: https://chroniclesofapragmaticprogrammer.substack.com/p/optimistic-locking

- ### Retry em caso de conflito
  - Se uma transação falhar por conflito de versão, você pode tentar novamente até que seja bem-sucedida.
  - Benefício: 
      - Permite que operações concorrentes sejam resolvidas sem falhar imediatamente.
```java
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
```
- ### Garantia de que saldo nunca ficará negativo
  - A lógica de negócio deve impedir que o saldo seja menor que zero, mesmo em cenários concorrentes.
```java
public void validBalance(BigDecimal amount) {
    if (this.balance.compareTo(amount) < 0) {
        throw new CardDomainException("SALDO_INSUFICIENTE");
    }
}
```

## 🗄️ Banco de dados
- ### SQL: MySQL
  - Configuração típica em application.yml:
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/miniautorizador?useSSL=false&serverTimezone=UTC
    username: root
    password:
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true
    properties:
      hibernate:
        dialect: org.hibernate.dialect.MySQL8Dialect
```

## 🧪 Testes
- ### Unitários: regras de negócio e invariantes
  - Teste de regras de negócio isoladas

- ### Concorrência: simulação de transações simultâneas
  - Simulação de transações concorrentes.

- ### Cobertura: alta cobertura, testes validando comportamento real
  - Jacoco → gera relatórios de cobertura automaticamente durante o build Maven.
  - SonarQube → analisa qualidade do código e integra com Jacoco para exibir métricas detalhadas.
> 📦 Jacoco no pom.xml
> 👉 Gera relatórios em target/site/jacoco/index.html

```xml
<build>
  <plugins>
    <!-- Plugin do Spring Boot -->
    <plugin>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-maven-plugin</artifactId>
    </plugin>

    <!-- Jacoco para cobertura -->
    <plugin>
      <groupId>org.jacoco</groupId>
      <artifactId>jacoco-maven-plugin</artifactId>
      <version>0.8.10</version>
      <executions>
        <execution>
          <goals>
            <goal>prepare-agent</goal>
          </goals>
        </execution>
        <execution>
          <id>report</id>
          <phase>verify</phase>
          <goals>
            <goal>report</goal>
          </goals>
        </execution>
      </executions>
    </plugin>
  </plugins>
</build>
```
> 📊 SonarQube  No pom.xml

```xml
<plugin>
  <groupId>org.sonarsource.scanner.maven</groupId>
  <artifactId>sonar-maven-plugin</artifactId>
  <version>3.9.1.2184</version>
</plugin>
```

- No application.properties ou sonar-project.properties:
```properties
sonar:
  projectKey: mini-autorizador
  host.url: http://localhost:9000
  login: squ_6e4249777aa58a4a0e57e9827166a399f26ca46c
```

- Rodar análise:
```bash
mvn clean verify sonar:sonar
```
## 🎯 Meta de cobertura
- Domínio e serviços: cobertura mínima de 80%.
- Testes reais: validação de comportamento de regras de negócio e concorrência, não apenas mocks.
---

## 📦 Como rodar
### Clone o repositório
```bash
git clone https://github.com/seu-usuario/mini-autorizador.git
cd mini-autorizador
```
### Suba os bancos com Docker Compose
```bash
docker-compose up -d
```

### Rode a aplicação
```bash
mvn spring-boot:run
```

### Acesse os endpoints em: ``` http://localhost:8080 ```

## 👨‍💻 Autor
Desenvolvido por [alansouz4](https://github.com/alansouz4) como parte do processo seletivo da VR Benefícios. 