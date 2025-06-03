# Docker Environment for OpenEPCIS

This folder contains Docker Compose setups to bootstrap the full OpenEPCIS infrastructure stack.

## Components

- **docker-compose.yml** — Launches Kafka, OpenSearch, OpenSearch Dashboards, and optionally the REST API (CE or RE).
- **.env** — Defines the `COMPOSE_PROJECT_NAME` and can be extended for environment configuration.

## Usage

### Setup using Podman

Start Community Edition REST API:

```shell
podman-compose -f docker-compose.rest-api-ce.yml up -d

# Run one-time Kafka topic setup
podman-compose -f docker-compose.kafka-setup.yml run --rm kafkasetup

# Restart REST API to pick up topics
podman restart quarkus-rest-api-ce

# check logs
podman logs --tail 250 -f quarkus-rest-api-ce
```

Or Research Edition

```shell
podman-compose -f docker-compose.rest-api-re.yml up -d

# Run one-time Kafka topic setup
podman-compose -f docker-compose.kafka-setup.yml run --rm kafkasetup

# Restart REST API to pick up topics
podman restart quarkus-rest-api-re

# check logs
podman logs --tail 250 -f quarkus-rest-api-re
```

### Setup using Docker

Start Community Edition REST API:

```shell
docker compose -f docker-compose.rest-api-ce.yml up -d

# Run one-time Kafka topic setup
docker compose -f docker-compose.kafka-setup.yml run --rm kafkasetup

# Restart REST API to pick up topics
podman restart quarkus-rest-api-ce

# check logs
docker logs --tail 250 -f quarkus-rest-api-ce
```

Or Research Edition

```shell
docker compose -f docker-compose.rest-api-re.yml up -d

# Run one-time Kafka topic setup
docker compose -f docker-compose.kafka-setup.yml run --rm kafkasetup

# Restart REST API to pick up topics
podman restart quarkus-rest-api-re

# check logs
docker logs --tail 250 -f quarkus-rest-api-re
```
