# Changelog

## [0.9.5]

- EPCIS 1.2 XML output carries only the EPCIS namespaces, no stray `xmlns:xalan` declaration.
- The OpenAPI document declares no security requirements, so the Swagger UI at `/q/swagger-ui`
  shows every endpoint as open and asks for no credentials.
- The service offers the EPCIS 2.0 REST binding. `GET /events/aggregate`, `POST /events/validate`
  and `PUT /queries/{queryName}` are part of the Business edition.
- Operations outside the standard, such as `/userExtension/jsonSchema` for your own extension
  schemas, are marked `x-openepcis-non-standard` in the OpenAPI document.
- Built on `io.openepcis` 0.9.5 from Maven Central.

## [0.9.4]

The first release of the Community Edition that builds from Maven Central alone.

- One Quarkus application assembled from `io.openepcis:quarkus-rest-application` and
  `io.openepcis:quarkus-capture-topology` 0.9.4, both Apache-2.0 on Maven Central.
- The copied application and topology sources, the Research Edition and the company Maven
  registry are gone. A Maven enforcer refuses any Business-edition artifact.
- No authentication: every caller is admin. For trying out, development and research, not for
  production; the Business edition is the one to run there.
- Since nobody logs in, the compose file publishes the service and OpenSearch on the loopback
  interface only, and the image allows browser calls (CORS) from localhost pages only
  (`QUARKUS_HTTP_CORS_ORIGINS` widens it).
- The extension schemas and JSON-LD contexts of ref.openepcis.org are in the image, so their
  events are captured offline and without registering a schema.
- One compose file with OpenSearch 2.19, Kafka 3.8 (KRaft, official image), the topics and the
  service, its port configurable with `OPENEPCIS_PORT`. Java 25, images for linux/amd64 and linux/arm64 on GHCR.
