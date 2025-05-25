# Docker Environment for OpenEPCIS

This folder contains Docker Compose setups to bootstrap the full OpenEPCIS infrastructure stack.

## Components

- **docker-compose.yml** — Launches Kafka, OpenSearch, OpenSearch Dashboards, and optionally the REST API (CE or RD).
- **.env** — Defines the `COMPOSE_PROJECT_NAME` and can be extended for environment configuration.

## Usage

Start core infrastructure:

```bash
docker compose up -d
```

Run one-time Kafka topic setup:

```bash
docker compose --profile init run --rm kafkasetup
```

Start REST API:

```bash
docker compose --profile rest-api-ce up -d   # Community Edition
docker compose --profile rest-api-re up -d   # Research Edition
```

> Podman is also supported: use `podman-compose` as a drop-in replacement.
