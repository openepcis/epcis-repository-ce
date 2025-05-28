# OpenEPCIS Examples

This folder contains real-world and synthetic EPCIS 2.0 event examples to help you get started with capturing and querying data using OpenEPCIS.

---

## What’s Included

* Minimal capture test flows using curl: *see [https://curl.se/](https://curl.se/)*
* Capturing examples with and without user extensions

---

## How to Use These Examples

### 1. Start OpenEPCIS Locally

Make sure OpenEPCIS and supporting services are running.  
See the [`/docker`](../docker/README.md) folder for quick setup using Docker Compose.

### 2. Optional: Register User Extension Schema

Some examples include custom fields under their own namespace.  
If needed, register a schema using the `/userExtension/jsonSchema` endpoint.  
See the [CAPTURE.md](getting-started/CAPTURE.md) guide for details.

### 3. Submit an Example Event

Use `curl` to post any example directly:

```bash
curl --header "Content-Type: application/ld+json" --data-binary @example_9.6.1-object_event.jsonld http://localhost:8080/capture
````

Or pipe directly from hosted GS1 Examples:

```bash
curl -o - "https://ref.gs1.org/docs/epcis/examples/example_9.6.1-object_event.jsonld" | \
curl --header "Content-Type: application/ld+json" http://localhost:8080/capture -d @-
```

---

## Recommended Tools

* [OpenEPCIS Tools](https://tools.openepcis.io) — Generate valid EPCIS test data
* [OpenEPCIS Swagger UI](https://tools.openepcis.io/q/swagger-ui) — API Explorer
