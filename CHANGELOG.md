# Changelog

## [0.9.4]

The first release of the Community Edition that builds from Maven Central alone.

- One Quarkus application assembled from `io.openepcis:quarkus-rest-application` and
  `io.openepcis:quarkus-capture-topology` 0.9.4, both Apache-2.0 on Maven Central.
- The copied application and topology sources, the Research Edition and the company Maven
  registry are gone. A Maven enforcer refuses any Business-edition artifact.
- Authentication is HTTP basic with `OPENEPCIS_ADMIN_PASSWORD` (no default), optionally bearer
  tokens of one OIDC realm. The previous image accepted every caller as admin.
- One compose file with OpenSearch 2.19, Kafka 3.8 (KRaft, official image), the topics and the
  service. Java 25, images for linux/amd64 and linux/arm64 on GHCR.
