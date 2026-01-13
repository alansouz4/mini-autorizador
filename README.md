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
    /model
    /service
    /ports
      AuthenticationPort.java   // contrato
  /application
    LoginUseCase.java           // caso de uso
  /infrastructure
    /auth
      SecurityConfig.java       // configuração técnica
```
### 🧾 Exemplo de requisição
1. Gerando o token
   - Se o usuário for admin e a senha 123, a string é: ``admin:123``
   - Codificada em Base64: ``YWRtaW46MTIz``

2. Usando no cabeçalho HTTP
   ``GET /api/contas HTTP/1.1
   Host: localhost:8080
   Authorization: Basic YWRtaW46MTIz``

3. Exemplo com curl
   ``curl -X GET "http://localhost:8080/api/contas" \
   -H "Authorization: Basic YWRtaW46MTIz"``

✅ Resumindo
- Cabeçalho: Authorization: Basic <Base64(username:password)>
- Exemplo: Authorization: Basic YWRtaW46MTIz
- Uso: Protege endpoints do mini-autorizador garantindo que apenas usuários autenticados acessem os recursos.

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
import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "cartao")
public record Cartao(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id,

    @Column(unique = true, nullable = false)
    String numeroCartao,

    @Column(nullable = false)
    String senha,

    @Column(nullable = false)
    BigDecimal saldo,

    @Version
    Long version
) {

    public Cartao debitar(BigDecimal valor) {
        if (saldo.compareTo(valor) < 0) {
        throw new IllegalStateException("SALDO_INSUFICIENTE");
    }
    return new Cartao(id, numeroCartao, senha, saldo.subtract(valor), version);
}

    public boolean validarSenha(String senhaInformada) {
        return this.senha.equals(senhaInformada);
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
public void autorizarTransacao(String numeroCartao, BigDecimal valor) {
    int tentativas = 0;
    boolean sucesso = false;

    while (!sucesso && tentativas < 3) {
        try {
            Cartao cartao = cartaoRepository.findByNumeroCartao(numeroCartao)
                    .orElseThrow(() -> new IllegalStateException("CARTAO_INEXISTENTE"));

            Cartao atualizado = cartao.debitar(valor);
            cartaoRepository.save(atualizado);

            sucesso = true;
        } catch (OptimisticLockException e) {
            tentativas++;
            if (tentativas == 3) {
                throw new IllegalStateException("CONCORRENCIA_DEBITO_FALHOU");
            }
        }
    }
}
```
- ### Garantia de que saldo nunca ficará negativo
  - A lógica de negócio deve impedir que o saldo seja menor que zero, mesmo em cenários concorrentes.
```java
public Cartao debitar(BigDecimal valor) {
    if (saldo.compareTo(valor) < 0) {
        throw new IllegalStateException("SALDO_INSUFICIENTE");
    }
    return new Cartao(id, numeroCartao, senha, saldo.subtract(valor), version);
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
    password: root
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true
    properties:
      hibernate:
        dialect: org.hibernate.dialect.MySQL8Dialect
```
- ### ACID, atomicidade no débito de saldo
  - Garantido pelo uso de transações JPA e optimistic locking com @Version.
  - Exemplo de update seguro:
```java
@Transactional
public void debitar(String numeroCartao, BigDecimal valor) {
    Cartao cartao = cartaoRepository.findByNumeroCartao(numeroCartao)
        .orElseThrow(() -> new IllegalStateException("CARTAO_INEXISTENTE"));

    Cartao atualizado = cartao.debitar(valor);
    cartaoRepository.save(atualizado);
}
```

## 🧪 Testes
- ### Unitários: regras de negócio e invariantes
  - Teste de regras de negócio isoladas:
```java
@Test
void deveDebitarSaldoCorretamente() {
    Cartao cartao = new Cartao(1L, "123456789", "1234", new BigDecimal("500.00"), 0L);
    Cartao atualizado = cartao.debitar(new BigDecimal("100.00"));
    assertEquals(new BigDecimal("400.00"), atualizado.saldo());
}
```  
  -
    - Teste de invariantes:
```java
@Test
void deveFalharAoDebitarSaldoInsuficiente() {
    Cartao cartao = new Cartao(1L, "123456789", "1234", new BigDecimal("500.00"), 0L);
    assertThrows(IllegalStateException.class, () -> cartao.debitar(new BigDecimal("600.00")));
}
```
- ### Integração: endpoints REST e persistência
  - Teste de persistência com banco em memória (H2) ou Testcontainers (MySQL real).
  - Teste de endpoints REST com MockMvc
```java
@Autowired
private MockMvc mockMvc;

@Test
void deveCriarCartaoComSucesso() throws Exception {
    mockMvc.perform(post("/cartoes")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"numeroCartao\":\"6549873025634501\",\"senha\":\"1234\"}"))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.numeroCartao").value("6549873025634501"));
}
```
- ### Concorrência: simulação de transações simultâneas
  - Simulação de transações concorrentes:

```java
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public class TransacaoConcorrenteTest {
    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void deveAutorizarTransacaoConcorrente() {
        // Criação de cartão
        Cartao cartao = new Cartao(1L, "123456789", "1234", new BigDecimal("500.00"), 0L);
        cartaoRepository.save(cartao);

        // Simulação de transações concorrentes
        List<Callable<Void>> tasks = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            tasks.add(() -> {
                restTemplate.postForEntity("http://localhost:8080/autorizar", 
                        new Transacao("123456789", "1234", new BigDecimal("100.00")), Void.class);
                return null;
            });
        }

        // Execução das tarefas
        ExecutorService executor = Executors.newFixedThreadPool(10);
        try {
            executor.invokeAll(tasks);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Verificação do saldo final
        Cartao cartaoAtualizado = cartaoRepository.findByNumeroCartao("123456789");
        assertEquals(new BigDecimal("400.00"), cartaoAtualizado.getSaldo());
    }
}
```
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
sonar.projectKey=mini-autorizador
sonar.host.url=http://localhost:9000
sonar.login=seu-token-sonar
```

- Rodar análise:
```bash
mvn clean verify sonar:sonar
```

---

### 🎯 Meta de cobertura
- Domínio e serviços: cobertura mínima de 80%.
- Testes reais: validação de comportamento de regras de negócio e concorrência, não apenas mocks.
- Exemplo de relatório Jacoco:
- Cartao.debitar() → 100% coberto.
- AutorizacaoService.autorizar() → 95% coberto.
- Controllers REST → cobertos via testes de integração.

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

## 🔮 Próximos passos (opcionais)
- Implementar idempotência em transações
- Adicionar auditoria e logs estruturados
- Observabilidade com métricas e tracing
- Testes de carga com K6/Gatling

## 👨‍💻 Autor
Desenvolvido por [alansouz4](https://github.com/alansouz4) como parte do processo seletivo da VR Benefícios. 