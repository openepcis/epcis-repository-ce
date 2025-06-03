# Docker Environment for OpenEPCIS

This folder contains Docker Compose setups to bootstrap the full OpenEPCIS infrastructure stack.

## Components

- **docker-compose.yml** — Launches Kafka, OpenSearch, OpenSearch Dashboards, and optionally the REST API (CE or RE).
- **.env** — Defines the `COMPOSE_PROJECT_NAME` and can be extended for environment configuration.

## Usage

### Setup using Podman

Start core infrastructure:

```shell
podman-compose -f docker-compose.yml up -d
```

Run one-time Kafka topic setup:

```shell
podman-compose -f docker-compose.kafka-setup.yml run --rm kafkasetup
```

Start Community Edition REST API:

```shell
podman-compose -f docker-compose.rest-api-ce.yml up -d   # Community Edition
```

Or Research Edition

```shell
podman-compose -f docker-compose.rest-api-re.yml up -d   # Research Edition
```

### Setup using Docker

Start core infrastructure:

```shell
docker compose up -d
```

Run one-time Kafka topic setup:

```shell
docker compose -f docker-compose.kafka-setup.yml run --rm kafkasetup
```

Start Community Edition REST API:

```shell
docker compose -f docker-compose.rest-api-ce.yml up -d   # Community Edition
```

Or Research Edition

```shell
docker compose -f docker-compose.rest-api-re.yml up -d   # Research Edition
```
