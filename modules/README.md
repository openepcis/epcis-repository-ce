# OpenEPCIS Modules

This directory contains core components that make up the OpenEPCIS processing and API stack.

## Main Modules

- **openepcis-client/**  
  REST + WebSocket client to interact with EPCIS APIs and event streams. Includes runtime and deployment variants.

- **openepcis-rest-api-common/**  
  Common interfaces, constants, and API definitions shared across OpenEPCIS modules.

- **openepcis-generated-events-capture/**  
  Tooling for generating synthetic EPCIS 2.0 events using Jinja templates (for testing and demos).

- **quarkus-capture-topology-ce/**  
  Kafka Streams-based capture pipeline for Community Edition event processing.

- **quarkus-rest-application-ce/**  
  The Quarkus-based REST API implementation used in the Community Edition.

