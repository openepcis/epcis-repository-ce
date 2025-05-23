# OpenEPCIS Community Edition

Welcome to the **Community Edition** of **OpenEPCIS** — an open-source, container-friendly implementation of the [GS1 EPCIS 2.0](https://www.gs1.org/standards/epcis) standard. This repository provides the foundational components you need to deploy, develop, or extend a modern EPCIS repository in line with current GS1 visibility standards.

---

## 📚 Table of Contents

- [What is EPCIS 2.0?](#what-is-epcis-20)
- [What is OpenEPCIS?](#what-is-openepcis)
- [Repository Structure](#repository-structure)
- [Getting Started](#getting-started)
    - [Option 1: Run via Podman (recommended for pure open source experience)](#option-1-run-via-podman-recommended-for-pure-open-source-experience)
    - [Option 2: Run via Docker](#option-2-run-via-docker)
    - [Option 3: Developer Mode (Quarkus)](#option-3-developer-mode-quarkus)
- [Sample Requests](#sample-requests)
- [Dashboards](#-dashboards)
    - [🔎 OpenSearch Dashboards](#-opensearch-dashboards)
    - [Grafana (Optional)](#grafana-optional)
- [Learn More](#learn-more)
- [Contributing](#-contributing)
- [License](#license)

---

## What is EPCIS 2.0?

**EPCIS (Electronic Product Code Information Services)** is a standard developed by GS1 to share event-based supply chain data. **Version 2.0** enhances interoperability, web-native communication, and scalability:

- ✅ Native JSON/JSON-LD support
- 🔁 Better modeling of IoT, serialization, and aggregation
- 🌐 Seamless integration with [GS1 Digital Link](https://www.gs1.org/standards/Digital-Link/)
- 📡 Event streams via RESTful APIs

---

## What is OpenEPCIS?

**OpenEPCIS** is a modern EPCIS 2.0 implementation designed for real-world usage and experimentation:

- Modular, reactive architecture using **Quarkus**
- Uses **Kafka** for scalable event pipelines
- Built-in **OpenSearch** support for fast querying
- REST API with JSON-LD and GS1-compliant semantics
- Ready-to-use with **Docker** or **Podman**
- Built-in support for event validation and profile enforcement

---

## Repository Structure

```
distributions/
├── community-edition/ # Quarkus-based implementation of the Community Edition REST API
├── research-edition/ # Experimental features and advanced modules under evaluation
├── pom.xml # Parent POM for distribution builds

docker/
├── docker-compose.yml # Compose setup for Kafka, OpenSearch, Dashboards, and REST APIs
├── .env # Contains environment variables such as project name

modules/
├── openepcis-client/ # Quarkus-based EPCIS client for REST and WebSocket interaction
│ ├── runtime/ # Core runtime code, REST/WebSocket client logic, and Dockerfiles
│ └── deployment/ # Quarkus extension deployment and health check integration
│
├── openepcis-rest-api-common/ # Common API interfaces used across modules
│
├── openepcis-generated-events-capture/ # Test data generator for synthetic EPCIS events using Jinja templates
│ ├── templates/ # Input templates for event generation
│ └── capture/ # Core logic for streaming and bulk capture operations
│
├── quarkus-capture-topology-ce/ # Kafka Streams-based capture pipeline for Community Edition
│
├── quarkus-rest-application-ce/ # Entry point for Quarkus REST API apps (Community Edition)

parent/
├── pom.xml # Parent POM defining shared dependencies, plugin versions, and build settings for all OpenEPCIS modules and distributions.
```

---

## Getting Started

You can run the Community Edition in two ways:

---

### Option 1: Run via Podman (recommended for pure open source experience)

#### Prerequisites

- [Podman](https://podman.io/)
- [podman-compose](https://github.com/containers/podman-compose)

#### Steps

```bash
# Start infrastructure
podman-compose up -d

# One-time Kafka topic setup
podman-compose --profile init run --rm kafkasetup

# Start the REST API (Community Edition)
podman-compose --profile rest-api-ce up -d

# Or run the Research Edition
podman-compose --profile rest-api-rd up -d
```

---

### Option 2: Run via Docker

#### Prerequisites

- [Docker](https://docs.docker.com/get-docker/)
- [Docker Compose](https://docs.docker.com/compose/)

#### Step-by-step

```bash
# Clone the repository
git clone https://github.com/openepcis/epcis-repository-ce.git
cd epcis-repository-ce

# Start infrastructure: Kafka, OpenSearch, Dashboards
docker compose up -d

# One-time Kafka topic setup
docker compose --profile init run --rm kafkasetup

# Start the REST API (Community Edition)
docker compose --profile rest-api-ce up -d
```

#### Optional: Start Research Edition

```bash
docker compose --profile rest-api-rd up -d
```

---

### Option 3: Developer Mode (Quarkus)

#### Prerequisites

- Java 21+
- Apache Maven
- Docker or Podman for running Kafka + OpenSearch

#### Steps

```bash
# Start dependencies
docker compose up -d
docker compose --profile init run --rm kafkasetup

# Launch Community Edition REST API in live reload mode
cd distributions/community-edition
mvn quarkus:dev
```

---

## Sample Requests

You can test ingestion with:

```bash
curl -X POST http://localhost:8080/capture \
     -H "Content-Type: application/json" \
     -d @modules/openepcis-generated-events-capture/src/test/resources/epcisEvent.json
```

Query events with:

```bash
curl http://localhost:8080/events
```

---

## Dashboards

OpenEPCIS integrates seamlessly with **OpenSearch**, giving you full access to indexed EPCIS 2.0 event data for querying and visualization.

### 🔎 OpenSearch Dashboards

Accessible at:

👉 [http://localhost:5601](http://localhost:5601)

**OpenSearch Dashboards** is a powerful, built-in tool for exploring your EPCIS data:

- Filter and search raw EPCIS event documents
- Inspect the full JSON structure of events
- Build custom visualizations and dashboards
- Monitor flows, event types, and traceability patterns over time

> While we don’t ship with predefined dashboards, OpenSearch Dashboards makes it easy to create your own — whether you're debugging, auditing, or showcasing supply chain data.

### Grafana (Optional)

Prefer **Grafana**? You can connect it to OpenSearch as a data source:

- Use the OpenSearch plugin or built-in support in recent Grafana versions
- Visualize EPCIS event trends with Grafana's rich panel system
- Combine EPCIS data with other sources (e.g., Prometheus, Jaeger) in unified views

Grafana is ideal if you're already using it in your observability stack or want high-end control over layout, alerts, and multi-source dashboards.

---

## Learn More

- [GS1 EPCIS 2.0 Standard](https://www.gs1.org/standards/epcis)
- [GS1 Core Business Vocabulary (CBV)](https://www.gs1.org/standards/epcis-and-cbv)
- [GS1 Digital Link](https://www.gs1.org/standards/Digital-Link/)
- [Quarkus](https://quarkus.io/)
- [Kafka](https://kafka.apache.org/)
- [OpenSearch](https://opensearch.org/)

---

## Contributing

We welcome community involvement!

- Fork this repo and submit a PR
- Open an issue for bugs or questions
- Use Discussions for ideas and feedback

---

## License

The **OpenEPCIS Community Edition** is released under the [Apache 2.0 License](LICENSE), allowing free use, modification, and distribution for non-restrictive applications.

> ⚠️ Note: The **SAX-based high-performance EPCIS XML-to-JSON converter**, used in the Research Edition, is **not** covered by the Apache 2.0 license. It is distributed under a separate commercial license by **benelog GmbH & Co. KG** and **not permitted for free production use**.

If you're interested in using the Research Edition or the high-performance converter in commercial environments, please [contact us](mailto:info@openepcis.io) for licensing options.
]()