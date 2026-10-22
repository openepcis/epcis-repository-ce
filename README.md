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

The Community Edition is single-tenant and has no document-level security. Every
authenticated caller sees all data in the cluster, and OpenSearch runs without its security
plugin. There is no multi-tenancy, no field-level security, no API keys, no onboarding, no
streaming SAX converter (XSLT does the format work), no Azure Blob storage, no GS1 Germany
synchronisation, and no GS1 Conformant Resolver. Those are the
[Business edition](https://openepcis.io/docs/platform-overview/feature-matrix), which builds
on the same artifacts. A Maven enforcer in this repository refuses any of them.

## Run it

Docker or Podman with Compose, about 8 GB of memory.

```bash
git clone https://github.com/openepcis/epcis-repository-ce.git
cd epcis-repository-ce/docker
export OPENEPCIS_ADMIN_PASSWORD="$(openssl rand -base64 24)"
docker compose up -d
```

The compose file starts OpenSearch, Kafka with the topics the pipeline needs, and the service
on port 8080. The admin password has no default, the service refuses to start without it.
Readiness is at `http://localhost:8080/q/health/ready` once Kafka Streams is running, the
OpenAPI at `/q/swagger-ui`.

### Capture an event

```bash
curl -u openepcis-admin:$OPENEPCIS_ADMIN_PASSWORD -i \
  -H 'Content-Type: application/json' \
  --data @../examples/getting-started/object-event.json \
  http://localhost:8080/capture
```

`202 Accepted` with a `Location` header naming the capture job. Poll it until `running` is
false; `success` tells you whether the document passed validation.

### Query it

```bash
curl -u openepcis-admin:$OPENEPCIS_ADMIN_PASSWORD http://localhost:8080/events
curl -u openepcis-admin:$OPENEPCIS_ADMIN_PASSWORD \
  -H 'Accept: application/xml' -H 'GS1-EPCIS-Version: 1.2.0' http://localhost:8080/events
```

The second call answers with an EPCIS 1.2 query document. More in
[examples/getting-started/CAPTURE.md](examples/getting-started/CAPTURE.md).

## Configuration

| Variable | Default | What it does |
|---|---|---|
| `OPENEPCIS_ADMIN_PASSWORD` | none, required | Password of the user `openepcis-admin` (roles `admin`, `query`, `capture`) |
| `OPENEPCIS_OIDC_ENABLED` | `false` | Accept bearer tokens of one OIDC realm |
| `OPENEPCIS_OIDC_AUTH_SERVER_URL` | empty | The realm, for example `https://auth.example/realms/epcis` |
| `OPENEPCIS_OIDC_CLIENT_ID` | `openepcis` | The client the tokens are issued for |
| `QUARKUS_OPENSEARCH_HOSTS` | `localhost:9200` | The OpenSearch cluster |
| `QUARKUS_OPENSEARCH_PROTOCOL` | `http` | `https` when the cluster has TLS |
| `QUARKUS_OPENSEARCH_USERNAME`, `QUARKUS_OPENSEARCH_PASSWORD` | empty | Credentials when the cluster has its security plugin |
| `KAFKA_BOOTSTRAP_SERVERS` | `localhost:9092` | The Kafka cluster |
| `STORAGE_LOCAL_PATH` | `/data/openepcis` | Where captured documents are kept |
| `OPENEPCIS_DEFAULT_GROUP` | `query` | The one group every record is written under |
| `JOBRUNR_DATABASE_TYPE` | `mem` | Scheduled subscriptions in memory, or `sql` with a datasource |
| `OTEL_SDK_DISABLED` | `true` | `false` with an OTLP endpoint for traces |

Further users go into a mounted `config/application.yml` next to the application, under
`quarkus.security.users.embedded`.

## Build from source

```bash
mvn -B clean verify
```

Java 25. The image is built with Jib:

```bash
mvn -B clean package -DskipTests -Dquarkus.container-image.build=true
```

## Security posture

The compose stack is a development and evaluation setup: OpenSearch without its security
plugin, Kafka in plaintext, all data visible to every authenticated caller. Do not expose it to
the public internet without a reverse proxy and TLS in front, and give OpenSearch and Kafka their
own protection where the deployment demands it. Report a vulnerability to info@openepcis.io.

## License

Apache License 2.0, see [LICENSE](LICENSE).
