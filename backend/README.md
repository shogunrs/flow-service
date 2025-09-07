# Flow Backend (Spring Boot)

Camadas + Adapters, preparados para trocar persistência e fila.

- api: Controllers REST, DTOs, validação (Bean Validation), OpenAPI
- application: Services, portas (ports) para Repositórios e EventPublisher
- domain: Entidades e VOs (sem dependências infra)
- infrastructure:
  - mongo: implementa Repos via Spring Data MongoDB
  - kafka: `EventPublisher` em Redpanda/Kafka
  - outbox: persistência + scheduler para publicação assíncrona

## Requisitos
- Java 21
- Maven 3.9+
- Docker (para `docker-compose.dev.yml`)

## Subir dependências (Mongo + Redpanda)

```
docker compose -f docker-compose.dev.yml up -d
```

- Redpanda Console: http://localhost:8081
- MongoDB: mongodb://localhost:27017

## Rodar a API

```
cd backend
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

API: http://localhost:8080

OpenAPI UI: http://localhost:8080/swagger-ui/index.html

## Notas
- EventPublisher usa outbox + publicação assíncrona em `flow.events.v1`.
- Troca futura para Debezium/CDC: substituir adapter do outbox sem tocar os services.
