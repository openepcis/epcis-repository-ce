# OpenEPCIS Examples

This folder holds an EPCIS 2.0 example and a guide to capturing and querying data with OpenEPCIS.

---

## What’s Included

* Minimal capture test flows using curl: *see [https://curl.se/](https://curl.se/)*
* Capturing examples with and without user extensions

---

## How to Use These Examples

### 1. Start OpenEPCIS Locally

Make sure OpenEPCIS and supporting services are running.  
See [Run it](../README.md#run-it) in the README: `cd docker && docker compose up -d`.

### 2. Optional: Register User Extension Schema

Some examples include custom fields under their own namespace.  
If needed, register a schema using the `/userExtension/jsonSchema` endpoint.  
See the [CAPTURE.md](getting-started/CAPTURE.md) guide for details.

### 3. Submit an Example Event

Use `curl` to post any example directly:

```bash
curl --header "Content-Type: application/json" --data-binary @getting-started/object-event.json http://localhost:8080/capture
```

Or pipe directly from hosted GS1 Examples:

```bash
curl -o - "https://ref.gs1.org/docs/epcis/examples/example_9.6.1-object_event.jsonld" | \
curl --header "Content-Type: application/ld+json" http://localhost:8080/capture -d @-
```

---

## Recommended Tools

* [OpenEPCIS Tools](https://tools.openepcis.io) — Generate valid EPCIS test data
* [OpenEPCIS Swagger UI](https://tools.openepcis.io/q/swagger-ui) — API Explorer
