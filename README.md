# CRM Service — Hinova Desafio Técnico

Serviço responsável pela criação e gestão de propostas comerciais.
Faz parte de uma plataforma SaaS composta por dois módulos independentes: **CRM** e **SIGN**.

## Arquitetura

```
hinova-crm-service  (porta 8080)
hinova-sign-service (porta 8081)
```

A comunicação entre os serviços é feita via **REST síncrono**.

**Fluxo completo:**
1. CRM cria proposta → status: `RASCUNHO`
2. CRM envia proposta para assinatura → chama SIGN via REST
3. SIGN cria contrato → status: `AGUARDANDO_ASSINATURA`
4. CRM atualiza proposta → status: `ENVIADA_PARA_ASSINATURA`
5. SIGN assina contrato → chama callback do CRM
6. CRM atualiza proposta → status: `ASSINADA`

## Tecnologias

- Java 21
- Spring Boot 4.0.3
- MySQL 8
- MapStruct 1.6.3
- Lombok
- SpringDoc OpenAPI (Swagger)
- JUnit 5 + Mockito

## Pré-requisitos

- Java 21+
- Maven
- MySQL rodando na porta 3306
- Sign Service rodando na porta 8081

## Como rodar

### 1. Configure o banco de dados

```sql
CREATE DATABASE IF NOT EXISTS crm_db;
```

### 2. Configure o `application.properties`

```properties
spring.datasource.username=seu_usuario
spring.datasource.password=sua_senha
```

### 3. Suba o Sign Service primeiro

```bash
cd ../hinova-sign-service
./mvnw spring-boot:run
```

### 4. Suba o CRM Service

```bash
./mvnw spring-boot:run
```

## Endpoints

| Método | URL | Descrição |
|--------|-----|-----------|
| `POST` | `/api/v1/propostas` | Criar nova proposta |
| `GET` | `/api/v1/propostas` | Listar todas as propostas |
| `GET` | `/api/v1/propostas/{id}` | Buscar proposta por ID |
| `POST` | `/api/v1/propostas/{id}/enviar` | Enviar proposta para assinatura |
| `POST` | `/api/v1/propostas/callback/assinatura/{propostaId}` | Callback do SIGN (contrato assinado) |

## Swagger

Acesse: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)

## Testes

```bash
./mvnw test
```

## Decisões Técnicas

- **REST síncrono:** suficiente para o escopo, simples e fácil de validar
- **MapStruct:** mapeamento entre DTOs e entidades em tempo de compilação (type-safe, sem reflexão)
- **Idempotência:** o status da proposta é validado antes de qualquer transição — evita reprocessamento duplicado
- **Banco separado por serviço:** `crm_db` e `sign_db` — cada serviço gerencia seus próprios dados de forma independente
- **Callback:** o SIGN notifica o CRM via endpoint de callback quando o contrato é assinado, mantendo os serviços desacoplados

