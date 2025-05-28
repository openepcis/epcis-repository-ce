# Capturing Events into OpenEPCIS

## Prepare for Capturing

The OpenEPCIS repository supports capturing EPCIS 2.0 events via a simple REST API. This guide walks you through setting up the environment, registering optional 'example' user extension schemas (used in many examples), capturing events, and verifying the results.

---

## 1. Set Up Your Local OpenEPCIS Environment

### Prepare Backend Services

Use the setup in [`/docker`](../../docker/README.md). It starts the minimal OpenEPCIS stack with:

* OpenSearch + Dashboards
* Kafka
* OpenEPCIS Repository (REST interface)

### Start Core Infrastructure

```bash
# Start OpenSearch, Dashboards, and Kafka
docker compose up -d

# Run one-time Kafka topic setup
docker compose --profile init run --rm kafkasetup
```

### Run OpenEPCIS Repository Services Locally

#### Option 1: Use Docker (Recommended)

Choose one of the following editions:

* **Research Edition** – SAX-based converter (high performance, minimal dependencies)
* **Community Edition** – XSLT-based converter (closer to EPCIS 1.2 XML structure)

```bash
# Start Research Edition (SAX-based)
docker compose --profile rest-api-re up -d
```

```bash
# OR: Start Community Edition (XSLT-based)
docker compose --profile rest-api-ce up -d
```

#### Option 2: Run from Source (for Development/Debug)

Each service can be launched individually from the `/distributions` directory. This is useful for debugging, live development, or customization.

---

### Endpoints of Interest

* Repository API Swagger UI: [http://localhost:8080/q/swagger-ui](http://localhost:8080/q/swagger-ui)
* Capture Endoint: [http://localhost:8080/capture](http://localhost:8080/capture)
* Events Endoint: [http://localhost:8080/events](http://localhost:8080/events)
* OpenSearch: [http://localhost:9200](http://localhost:9200)
* OpenSearch Dashboards: [http://localhost:5601](http://localhost:5601)

---

## 2. Register a User Extension Schema (Required for Most 'Official' Examples)

Many examples from GS1 use custom namespaces for extensions. If your events include such extensions, you’ll need to register a JSON Schema.

```bash
curl -v --location 'http://localhost:8080/userExtension/jsonSchema?namespace=http%3A%2F%2Fns.example.com%2Fepcis%2F&defaultPrefix=example' \
--header 'Content-Type: application/json' \
--data '{
  "$schema": "http://json-schema.org/draft-07/schema#",
  "title": "Example",
  "additionalProperties": true,
  "properties": {
    "myField": { "type": "string" },
    "furtherSensorData": {
      "type": "array",
      "items": {
        "type": "object",
        "properties": {
          "measure1": { "type": "string" },
          "measure2": { "type": "string" }
        }
      }
    },
    "someFurtherMetaData": { "type": "string" },
    "cv": { "type": "string" }
  }
}'
```

If your events don’t use any custom extensions, you can skip this step.

---

## 3. Generate and Capture Example Events

Use the OpenEPCIS test data generator to create a few valid events and send them directly to the repository:

```bash
curl --header "Content-Type: application/json" "https://tools.openepcis.io/api/generateTestData?pretty=true" -d '
{
    "events": [
        {
            "nodeId": 1,
            "eventType": "ObjectEvent",
            "eventCount": 25,
            "locationPartyIdentifierSyntax": "WebURI",
            "dlURL": "https://id.gs1.org",
            "seed": 1748412892328,
            "ordinaryEvent": true,
            "action": "ADD",
            "eventID": false,
            "eventTime": {
                "timeZoneOffset": "+02:00",
                "fromTime": "2025-01-01T08:14:52+02:00",
                "toTime": "2025-05-28T08:14:52.000+02:00"
            },
            "businessStep": "COMMISSIONING",
            "disposition": "ACTIVE",
            "certificationInfo": [
                {
                    "extensionID": 0,
                    "prefix": "",
                    "contextURL": "",
                    "children": []
                }
            ],
            "userExtensions": [
                {
                    "extensionID": 8059,
                    "prefix": "",
                    "contextURL": "",
                    "children": []
                }
            ],
            "ilmd": [
                {
                    "extensionID": 7040,
                    "prefix": "",
                    "contextURL": "",
                    "children": []
                }
            ],
            "referencedIdentifier": [
                {
                    "identifierId": 1,
                    "epcCount": 1,
                    "classCount": 0
                }
            ],
            "parentReferencedIdentifier": {},
            "outputReferencedIdentifier": []
        }
    ],
    "identifiers": [
        {
            "identifierId": 1,
            "objectIdentifierSyntax": "WebURI",
            "dlURL": "https://id.gs1.org",
            "instanceData": {
                "sgtin": {
                    "identifierType": "sgtin",
                    "gcpLength": "",
                    "serialType": "range",
                    "sgtin": "09526545673796",
                    "rangeFrom": 1000000,
                    "count": 1,
                    "ID": 1
                }
            },
            "classData": null,
            "parentData": null
        }
    ],
    "randomGenerators": [
        {
            "name": "RND_0-1",
            "minValue": 0,
            "maxValue": 1,
            "meanValue": 0.5,
            "seedValue": 1747115247162,
            "distributionType": "TriangularDistribution",
            "randomID": 1
        }
    ],
    "contextUrls": []
}' -o - | curl --header "Content-Type: application/ld+json" "http://localhost:8080/capture" -d @-
```

---

## 4. Use Official GS1 JSON-LD Examples (Optional)

Alternatively, you can post directly from GS1’s reference event files. These are available at:

[https://ref.gs1.org/docs/epcis/examples](https://ref.gs1.org/docs/epcis/examples)

Examples:

```bash
curl -o - "https://ref.gs1.org/docs/epcis/examples/example_9.6.1-object_event.jsonld" | \
curl --header "Content-Type: application/ld+json" "http://localhost:8080/capture" -d @-

curl -o - "https://ref.gs1.org/docs/epcis/examples/sensor_data_example1.jsonld" | \
curl --header "Content-Type: application/ld+json" "http://localhost:8080/capture" -d @-
```

---

## 5. Query OpenSearch to Verify the Results

You can query OpenSearch directly to confirm the data was captured:

```bash
curl --header "Content-Type: application/json" "http://localhost:9200/epcis-event*/_search" -d '{
  "query": {
    "match_all": {}
  },
  "size": 100
}'
```

Or use the Dashboards UI at [http://localhost:5601](http://localhost:5601) to inspect the `epcis-event*` index.

---

## Further Resources

* [OpenEPCIS Tools](https://tools.openepcis.io) – Tools to generate and inspect EPCIS data
* [OpenEPCIS Swagger UI](https://tools.openepcis.io/q/swagger-ui) – API reference for the test data generator
* [GS1 EPCIS 2.0 Examples](https://ref.gs1.org/docs/epcis/examples/) – JSON-LD reference files
* [GS1 EPCIS Sandbox](https://epcis-sandbox.gs1.org/) – GS1-hosted demo environment
* [OpenEPCIS Event Sentry (Validator)](https://github.com/openepcis/openepcis-event-sentry) – Profile validation for captured events
