<p align="center">
  <img src="https://openepcis.io/img/openepcis-logo.svg" alt="OpenEPCIS" width="30%">
</p>

[![License](https://img.shields.io/badge/License-Apache_2.0-blue.svg)](LICENSE)
[![Build](https://github.com/openepcis/epcis-repository-ce/actions/workflows/build.yml/badge.svg)](https://github.com/openepcis/epcis-repository-ce/actions/workflows/build.yml)

<h1 align="center">OpenEPCIS EPCIS Repository, Community Edition</h1>

The EPCIS 2.0 repository of [OpenEPCIS](https://openepcis.io) as one container, under
Apache-2.0: capture, query with named queries, streaming and scheduled subscriptions, on
OpenSearch and Kafka. It is assembled from the artifacts of
[openepcis-core](https://github.com/openepcis/openepcis-core) and
[openepcis-rest-quarkus](https://github.com/openepcis/openepcis-rest-quarkus) on Maven Central,
and nothing in this build comes from anywhere else.

## What it is, and what it is not

The Community Edition is for trying OpenEPCIS out, for development and for research. **It has
no authentication: every caller is admin**, with the roles `capture`, `query` and `admin`, and
sees all data. OpenSearch runs without its security plugin and Kafka in plaintext. Nothing in
this edition is hardened, and it is **not meant for production**.

It is single-tenant. There is no login, no OIDC, no multi-tenancy, no document- or field-level
security, no capture checks (possession, commissioning rules, webhook destinations), no API keys,
no onboarding, no streaming SAX converter (XSLT does the format work), no Azure Blob storage, no
GS1 Germany synchronisation and no GS1 Conformant Resolver. Those are the
[Business edition](https://openepcis.io/docs/platform-overview/feature-matrix), which builds on
the same artifacts and is the one to run in production. A Maven enforcer in this repository
refuses any Business-edition artifact.

The extension schemas and JSON-LD contexts of the OpenEPCIS DPP extensions on
[ref.openepcis.org](https://ref.openepcis.org) are part of the image, so events using them are
captured without registering anything and without network access.

## Run it

Docker or Podman with Compose, about 8 GB of memory.

```bash
git clone https://github.com/openepcis/epcis-repository-ce.git
cd epcis-repository-ce/docker
docker compose up -d
```

The compose file starts OpenSearch, Kafka with the topics the pipeline needs, and the service on
port 8080 of the loopback interface only, OpenSearch on 9200 likewise
(`OPENEPCIS_PORT=18080 docker compose up -d` when 8080 is taken). Readiness is at
`http://localhost:8080/q/health/ready` once Kafka Streams is running, the OpenAPI at
`/q/swagger-ui`.

### Capture an event

```bash
curl -i -H 'Content-Type: application/json' \
  --data @../examples/getting-started/object-event.json \
  http://localhost:8080/capture
```

`202 Accepted` with a `Location` header naming the capture job. Poll it until `running` is
false; `success` tells you whether the document passed validation, `errors` why not.

### Query it

```bash
curl http://localhost:8080/events
curl -H 'Accept: application/xml' -H 'GS1-EPCIS-Version: 1.2.0' http://localhost:8080/events
```

The second call answers with an EPCIS 1.2 query document. More in
[examples/getting-started/CAPTURE.md](examples/getting-started/CAPTURE.md).

## Configuration

| Variable | Default | What it does |
|---|---|---|
| `OPENEPCIS_PORT` | `8080` | Host port of the service (compose file only) |
| `QUARKUS_OPENSEARCH_HOSTS` | `localhost:9200` | The OpenSearch cluster |
| `QUARKUS_OPENSEARCH_PROTOCOL` | `http` | `https` when the cluster has TLS |
| `QUARKUS_OPENSEARCH_USERNAME`, `QUARKUS_OPENSEARCH_PASSWORD` | empty | Credentials when the cluster has its security plugin |
| `KAFKA_BOOTSTRAP_SERVERS` | `localhost:9092` | The Kafka cluster |
| `STORAGE_LOCAL_PATH` | `/data/openepcis` | Where captured documents are kept |
| `QUARKUS_HTTP_CORS_ORIGINS` | localhost and 127.0.0.1, any port | Origins a browser may call the API from |
| `OTEL_SDK_DISABLED` | `true` | `false` with an OTLP endpoint for traces |

## Build from source

```bash
./mvnw -B clean verify
```

Java 25. The image is built with Jib:

```bash
./mvnw -B clean package -DskipTests -Dquarkus.container-image.build=true
```

This builds an image for linux/amd64 into the local Docker daemon; on an ARM machine add
`-Dquarkus.jib.platforms=linux/arm64`. The published image covers both.

## Security posture

None, by design. Anyone who reaches the port can capture, query and subscribe as admin, and every
subscription can call any URL. The compose file therefore publishes the service and OpenSearch on
the loopback interface only, and the image lets browsers call the API from localhost pages only
(CORS), so a web page from elsewhere cannot use it through your browser. Scheduled subscriptions
are kept in memory and are gone after a restart. Run it on your own machine or in a closed
network, never on the public internet. For production, use the Business edition. Report a vulnerability of the
shared code to info@openepcis.io.

## License

Apache License 2.0, see [LICENSE](LICENSE).
