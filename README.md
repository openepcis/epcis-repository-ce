# OpenEPCIS – Community Edition

Welcome to the **Community Edition** of **OpenEPCIS** — an open-source, container-friendly implementation of the [GS1 EPCIS 2.0](https://www.gs1.org/standards/epcis) standard. This project provides a modern foundation for deploying, developing, or extending EPCIS repositories in line with GS1 visibility standards.

> ✅ Optimized for JSON-LD, RESTful APIs, and real-time event capture  
> 🧪 Includes both stable (Community) and experimental (Research) editions  
> 🔧 Built on Kafka, Quarkus, OpenSearch, and container-native tooling

---

## 📚 Table of Contents

- [What is EPCIS 2.0?](#what-is-epcis-20)
- [What is OpenEPCIS?](#what-is-openepcis)
- [Repository Structure](#repository-structure)
- [Getting Started](#getting-started)
  - [Option 1: Run via Podman](#option-1-run-via-podman)
  - [Option 2: Run via Docker](#option-2-run-via-docker)
  - [Option 3: Developer Mode (Quarkus)](#option-3-developer-mode-quarkus)
- [Ready to Try Event Capture?](#-ready-to-try-event-capture)
- [Sample Requests](#sample-requests)
- [Dashboards](#dashboards)
  - [OpenSearch Dashboards](#opensearch-dashboards)
  - [Grafana (Optional)](#grafana-optional)
- [Learn More](#learn-more)
- [Contributing](#contributing)
- [License](#license)

---

## What is EPCIS 2.0?

**EPCIS (Electronic Product Code Information Services)** is a GS1 standard for sharing supply chain event data. Version 2.0 introduces major improvements:

- ✅ Native JSON/JSON-LD support
- 🌐 Better interoperability with web services
- 📡 Event-based RESTful communication
- 🔁 Enhanced modeling for serialized products, sensor data, and IoT
- 🔗 Seamless integration with [GS1 Digital Link](https://www.gs1.org/standards/digital-link/)

---

## What is OpenEPCIS?

**OpenEPCIS** is a modular, scalable EPCIS 2.0 repository built for production and experimentation.

- Fast, reactive architecture using **Quarkus**
- Kafka-powered **event pipelines**
- Built-in **OpenSearch** integration for queries
- RESTful API supporting EPCIS 2.0 (JSON-LD)
- Native support for custom **extension schemas**
- Runs with **Docker**, **Podman**, or as a native Java app

---

## Repository Structure

```

distributions/
├── community-edition/            # REST API (Community Edition)
├── research-edition/             # REST API (Research Edition)

docker/
├── docker-compose.yml            # Infra stack (Kafka, OpenSearch, Dashboards)
├── .env                          # Environment variables

modules/
├── openepcis-rest-api-common/    # Shared API components
├── openepcis-client/             # REST/WebSocket client
├── openepcis-generated-events-capture/  # Synthetic test data generator
├── quarkus-capture-topology-ce/  # Kafka capture stream (CE)
├── quarkus-rest-application-ce/  # Community Edition entrypoint

````

---

## Getting Started

To launch the minimal OpenEPCIS stack locally:

1. Clone the repository
2. Navigate to the `docker` directory
3. Start the services using Docker Compose

```shell
git clone https://github.com/openepcis/epcis-repository-ce.git
cd epcis-repository-ce/docker
```

For details on what services are included and how to configure them, refer to the [docker/README.md](docker/README.md).

### Option 1: Run via Podman

> Best for pure open-source environments.

**Prerequisites:**

- [Podman](https://podman.io/)
- [podman-compose](https://github.com/containers/podman-compose)

```bash
podman-compose up -d                          # Start infrastructure
podman-compose --profile init run --rm kafkasetup

podman-compose up quarkus-rest-api-ce         # Start Community Edition
# or:
podman-compose up quarkus-rest-api-re         # Start Research Edition
````

---

### Option 2: Run via Docker

**Prerequisites:**

* [Docker](https://docs.docker.com/get-docker/)
* [Docker Compose](https://docs.docker.com/compose/)

```bash
docker compose up -d                          # Start Kafka, OpenSearch, Dashboards
docker compose --profile init run --rm kafkasetup

docker compose up quarkus-rest-api-ce         # Start Community Edition
# or:
docker compose up quarkus-rest-api-re         # Start Research Edition
```

---

### Option 3: Developer Mode (Quarkus)

> Run locally using Maven + hot reload.

**Prerequisites:**

* Java 21+
* Apache Maven
* Docker or Podman (for dependencies)

```bash
docker compose up -d
docker compose --profile init run --rm kafkasetup

cd distributions/community-edition
mvn quarkus:dev
```

---

### 🚀 Ready to Try Event Capture?

Take a look at the full [capture walkthrough](examples/getting-started/CAPTURE.md) to learn how to:

* Register extension schemas for custom EPCIS fields
* Send test events using `curl` or the built-in generator
* Verify captured data in OpenSearch and Dashboards

It’s the fastest way to see OpenEPCIS in action.

---

## Sample Requests

Use `curl` to test the capture endpoint:

```bash
curl -X POST http://localhost:8080/capture \
     -H "Content-Type: application/json" \
     -d @modules/openepcis-generated-events-capture/src/test/resources/epcisEvent.json
```

Query captured events:

```bash
curl http://localhost:8080/events
```

---

## Dashboards

OpenEPCIS uses **OpenSearch** for indexing and provides tools for visual traceability.

### OpenSearch Dashboards

Visit: [http://localhost:5601](http://localhost:5601)

* Filter and inspect raw event data
* Monitor by business step, disposition, product
* Build custom charts and dashboards

> Dashboards are not shipped by default — you can build them using `epcis-event*` indices.

---

### Grafana (Optional)

You can connect **Grafana** to OpenSearch:

* Use OpenSearch plugin or native support
* Combine EPCIS with other telemetry data
* Build alerts or cross-supply chain views

---

## Learn More

* [GS1 EPCIS 2.0 Standard](https://ref.gs1.org/standards/epcis/)
* [GS1 Core Business Vocabulary (CBV)](https://ref.gs1.org/standards/cbv/)
* [GS1 Digital Link](https://ref.gs1.org/standards/digital-link/)
* [Quarkus](https://quarkus.io/)
* [Apache Kafka](https://kafka.apache.org/)
* [OpenSearch](https://opensearch.org/)
* [OpenEPCIS Tools](https://tools.openepcis.io)
* [Event Sentry Validator](https://github.com/openepcis/openepcis-event-sentry)

---

## Contributing

We welcome your feedback and contributions.

* Fork and submit a PR
* Open issues for bugs or enhancements
* Join discussions in GitHub

---

## License

The **OpenEPCIS Community Edition** is licensed under the [Apache 2.0 License](LICENSE).

> ⚠️ Note: The **Research Edition** includes a SAX-based XML converter and other components distributed under a separate license by **benelog GmbH & Co. KG**.
> Commercial usage of these components requires a separate agreement.
> Contact: [info@openepcis.io](mailto:info@openepcis.io)
