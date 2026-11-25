# Changelog

## [0.9.4]

The first release of the Community Edition that builds from Maven Central alone.

- One Quarkus application assembled from `io.openepcis:quarkus-rest-application` and
  `io.openepcis:quarkus-capture-topology` 0.9.4, both Apache-2.0 on Maven Central.
- The copied application and topology sources, the Research Edition and the company Maven
  registry are gone. A Maven enforcer refuses any Business-edition artifact.
- No authentication: every caller is admin. For trying out, development and research, not for
  production; the Business edition is the one to run there. The previous image said otherwise
  and shipped proprietary code under the Apache label.
- The extension schemas and JSON-LD contexts of ref.openepcis.org are in the image, so their
  events are captured offline and without registering a schema.
- One compose file with OpenSearch 2.19, Kafka 3.8 (KRaft, official image), the topics and the
  service, its port configurable with `OPENEPCIS_PORT`. Java 25, images for linux/amd64 and linux/arm64 on GHCR.
