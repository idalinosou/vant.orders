# vant.orders

[![CI](https://github.com/idalinosou/vant.orders/actions/workflows/ci.yml/badge.svg)](https://github.com/idalinosou/vant.orders/actions/workflows/ci.yml)

Pedidos com cotação de frete assíncrona: a API grava o pedido e publica um evento na fila; um worker em TypeScript consome a mensagem, calcula o frete e devolve a cotação pela própria API.

**Demonstração:** https://idalinosou.github.io/vant.orders/

A demonstração no GitHub Pages roda sem servidor: a interface entra em modo demonstração, com pedidos de exemplo e cotação simulada no navegador. A API real, o RabbitMQ e o worker sobem juntos pelo Docker Compose.

O projeto existe para mostrar, de ponta a ponta, o stack usado em vagas back-end: Java com Spring Boot, Node.js com TypeScript, Angular, OpenAPI, Oracle/SQL Server, Docker, Jenkins, Maven, mensageria, cloud e CI/CD.

## Stack em uso

| Requisito | Como aparece no projeto |
| --- | --- |
| Java com Spring Boot | `backend/` — Spring Boot 3.3, Java 21, Spring Web, Data JPA, Validation, Actuator |
| Node.js e TypeScript | `worker/` — Node 24, ESM, TypeScript estrito, `node --test` |
| Angular | `frontend/` — Angular 22 standalone, signals, `@if`/`@for`, HttpClient |
| Swagger/OpenAPI | springdoc em `config/OpenApiConfig.java`, UI em `/swagger-ui.html`, JSON em `/v3/api-docs` |
| Oracle e SQL Server | perfis `oracle` e `sqlserver` + migrações Flyway por fornecedor em `backend/src/main/resources/db/migration/{oracle,sqlserver}` |
| Docker | `backend/Dockerfile`, `worker/Dockerfile`, `frontend/Dockerfile`, `docker-compose.yml` |
| Jenkins e CI/CD | `Jenkinsfile` e `.github/workflows/ci.yml` (o build também publica o front no Pages) |
| Maven | `backend/pom.xml` (dependências, plugins, surefire) |
| Mensageria e integração | RabbitMQ no `backend/.../messaging`, consumo e callback no `worker/` |
| Cloud Computing | Perfis por ambiente, variáveis de ambiente, healthcheck e imagens stateless (mapeamento AWS/Azure abaixo) |
| AWS e/ou Azure | Mapa de implantação documentado na seção "Cloud", sem deploy feito |

## Arquitetura

```
Angular (SPA)
    │  HTTP JSON
    ▼
API Spring Boot ──── JPA/Flyway ───► H2 | SQL Server | Oracle
    │
    │ publica event "order.created" (exchange orders.exchange)
    ▼
RabbitMQ (fila freight.queue)
    │
    ▼
Worker Node + TypeScript ──► calcula frete
    │
    │ POST /api/orders/{id}/quote  (header X-Internal-Token)
    ▼
API Spring Boot ──► pedido passa de CREATED para QUOTED
```

Fluxo de status: `CREATED → QUOTED → DISPATCHED → DELIVERED`, com transições validadas em `OrderStatus.canMoveTo`.

## Como rodar

Pré-requisitos: Java 21, Maven 3.9+, Node 22+ (recomendado 24) e, opcionalmente, RabbitMQ.

### 1. API (H2 em memória)

```bash
cd backend
mvn spring-boot:run
```

- API: http://localhost:8080/api/orders
- Swagger: http://localhost:8080/swagger-ui.html
- Health: http://localhost:8080/actuator/health

O perfil padrão é o `local`: H2 em memória, Flyway aplicado do zero e quatro pedidos de exemplo criados apenas quando a tabela está vazia.

### 2. Worker (opcional sem RabbitMQ)

```bash
cd worker
npm ci
npm run build
npm start
```

Sem RabbitMQ a API apenas registra o evento no log (perfil `!rabbit`), então o pedido permanece em `CREATED`.

### 3. RabbitMQ e fluxo completo

```bash
docker run -d --name vant-orders-rabbit -p 5672:5672 -p 15672:15672 rabbitmq:3.13-management
cd backend && mvn spring-boot:run -Dspring-boot.run.profiles=local,rabbit
cd worker && npm start
```

Painel do RabbitMQ: http://localhost:15672 (guest/guest).

### 4. Interface

```bash
cd frontend
npm ci
npm start
```

Abre em http://localhost:4200. Sem API respondendo, a interface entra em **modo demonstração** com dados locais e simula a cotação após criar um pedido — útil para a versão publicada no GitHub Pages.

## Docker Compose

```bash
cp .env.example .env
docker compose up -d --build
```

- Interface: http://localhost:8081
- API: http://localhost:8080
- RabbitMQ: http://localhost:15672

O `nginx` da interface encaminha `/api`, `/swagger-ui`, `/v3/api-docs` e `/actuator` para o contêiner da API.

### Com SQL Server

```bash
docker compose -f docker-compose.yml -f docker/compose-sqlserver.yml --profile sqlserver up -d --build
```

O serviço `sqlserver-init` cria o banco `orders` antes da API subir; o Flyway aplica a migração em `db/migration/sqlserver`.

### Com Oracle

```bash
docker compose -f docker-compose.yml -f docker/compose-oracle.yml --profile oracle up -d --build
```

Usa `gvenzl/oracle-free:23-slim` com usuário `orders` e migração em `db/migration/oracle`.

## Bancos e migrações

| Perfil | URL padrão | Migração |
| --- | --- | --- |
| `local` | `jdbc:h2:mem:orders` | `db/migration/h2` |
| `sqlserver` | `jdbc:sqlserver://localhost:1433;databaseName=orders` | `db/migration/sqlserver` |
| `oracle` | `jdbc:oracle:thin:@//localhost:1521/FREEPDB1` | `db/migration/oracle` |

Tudo é configurado por variável de ambiente (`DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USER`, `DB_PASSWORD`, `DB_SERVICE`), sem alterar código entre ambientes.

## API

| Método | Rota | Descrição |
| --- | --- | --- |
| `POST` | `/api/orders` | Cria o pedido e publica `order.created` |
| `GET` | `/api/orders?status=&term=` | Lista com filtro por status e busca por código, cliente ou CEP |
| `GET` | `/api/orders/{id}` | Detalhe |
| `GET` | `/api/orders/summary` | Contagem por status |
| `POST` | `/api/orders/{id}/quote` | Cotação enviada pelo worker; exige `X-Internal-Token` |
| `PATCH` | `/api/orders/{id}/status` | Avança o status |

Erros seguem o `ProblemDetail` do Spring (`type`, `title`, `status`, `detail`), incluindo a lista de campos inválidos em `campos`.

## Regra de frete

Implementada em `worker/src/freight.ts` e replicada no modo demonstração da interface:

- base de R$ 18,00 + R$ 0,85 por kg;
- fator por região do CEP de destino (1,00 a 1,75);
- transportadora pela faixa de peso (até 300 kg, até 1000 kg, acima de 1000 kg);
- prazo estimado de 2 a 8 dias, conforme a distância entre as regiões.

## Testes

```bash
cd backend  && mvn verify          # 5 testes de integração (MockMvc + H2)
cd worker   && npm test            # 6 testes da regra de frete
cd frontend && npm test            # teste de renderização do componente raiz
```

## CI/CD

- `.github/workflows/ci.yml`: em cada push/PR roda `mvn verify`, build e testes do worker, testes e build do Angular.
- `.github/workflows/deploy-pages.yml`: publica a interface no GitHub Pages com `--base-href /vant.orders/`.
- `Jenkinsfile`: pipeline declarativo com as mesmas etapas e build das imagens Docker na branch `main`. Precisa de um servidor Jenkins com as ferramentas `maven-3.9`, `jdk-21` e `node-24` cadastradas.

## Cloud

O projeto é stateless o suficiente para contêiner: configuração por variável de ambiente, healthcheck em `/actuator/health` e banco externo por perfil. Um mapeamento direto:

| Peça | AWS | Azure |
| --- | --- | --- |
| API e worker | ECS Fargate (ou App Runner) | Container Apps |
| Interface | S3 + CloudFront | Static Web Apps |
| Banco | RDS for SQL Server / Oracle Database@AWS | Azure SQL / Oracle Database@Azure |
| Fila | Amazon MQ for RabbitMQ | Azure Service Bus (ou RabbitMQ em VM) |
| Segredos | Secrets Manager | Key Vault |
| Pipeline | CodePipeline + CodeBuild | Azure DevOps Pipelines |

Nada foi implantado em nuvem; o mapeamento é o próximo passo natural a partir do compose.

## Estrutura

```
backend/    API Spring Boot (Java 21, Maven, Flyway, springdoc)
worker/     consumidor RabbitMQ em Node + TypeScript
frontend/   SPA Angular com modo demonstração
docker/     overrides de compose para SQL Server e Oracle
.github/    workflows de CI e deploy do Pages
Jenkinsfile pipeline de CI/CD
```

## Limitações deste ambiente

- Docker não está instalado na máquina onde o projeto foi montado: os Dockerfiles e o compose foram escritos e revisados, mas não executados. O mesmo vale para o Jenkins.
- SQL Server e Oracle foram configurados e têm migrações próprias, porém só o caminho H2 foi executado.
- O worker foi compilado e testado; o consumo real de RabbitMQ depende do broker rodando.

vant.
