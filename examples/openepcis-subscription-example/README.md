Here’s a comprehensive `README.md` for your project:

---

# OpenEPCIS Subscription Example

This project demonstrates how to use the **OpenEPCIS Client** to subscribe to EPCIS events using a reactive and WebSocket-based approach. It is part of the OpenEPCIS repository ecosystem and showcases how to build a subscription-based EPCIS client for processing real-time EPCIS event data.

## Table of Contents

- [Overview](#overview)
- [Requirements](#requirements)
- [Installation](#installation)
- [Running the Application](#running-the-application)
- [Project Structure](#project-structure)
- [Usage](#usage)
- [Configuration](#configuration)
- [Building Native Images](#building-native-images)
- [License](#license)

## Overview

This example project demonstrates how to:
- Connect to an EPCIS repository via WebSocket using `openepcis-client`.
- Process EPCIS events such as `ObjectEvent` and query for related `AggregationEvent` data.
- Build a reactive EPCIS client that supports continuous streams of event data.

## Requirements

- **JDK 21**
- **Maven 3.8+**
- **Quarkus** (builds the application with native-image support)
- **Docker** (optional for building native images)

## Installation

1. **Clone the repository:**

   ```bash
   git clone https://github.com/openepcis/epcis-repository-ce
   ```

2. **Build the project using Maven:**

   ```bash
   mvn clean install
   ```

3. **Ensure the OpenEPCIS server is running** and accessible for WebSocket connections.

## Running the Application

To run the application in **JVM mode**, execute the following command:

```bash
cd examples/openepcis-subscription-example
mvn quarkus:dev
```

Once started, the application will:
- Connect to an EPCIS WebSocket server.
- Subscribe to a predefined EPCIS query (`shippingSample01`).
- Process incoming `EpcisQueryResult` events.

**Application startup logs:**

```
The application is starting...
WebSocket session opened
got 5 event(s) from subscription
getting AggregationEvents with parentID for urn:epc:id:sscc:1234567890
childEPCs: [urn:epc:id:sgtin:123456789012.1, urn:epc:id:sgtin:123456789012.2]
```

To stop the application, press `Ctrl+C`.

## Project Structure

```bash
.
├── README.md
├── mvnw
├── mvnw.cmd
├── pom.xml
└── src
    └── main
        ├── docker
        │ ├── Dockerfile.jvm
        │ ├── Dockerfile.legacy-jar
        │ ├── Dockerfile.native
        │ └── Dockerfile.native-micro
        └── java
            └── io
                └── openepcis
                    └── example
                        ├── ApplicationLifeCycle.java
                        └── EPCISQueryResultProcessor.java
```

### Key Components:
- **`ApplicationLifeCycle`**: Handles startup and shutdown events to manage WebSocket connections.
- **`EPCISQueryResultProcessor`**: Processes `EpcisQueryResult` events and performs additional queries for related events.

## Usage

1. Configure the WebSocket server and query parameters in `application.properties` (see Configuration section).
2. Start the application.
3. Check the logs for processed EPCIS events.

## Configuration

The project uses the OpenEPCIS `openepcis-client` to connect to an EPCIS server. Configure the following properties in `application.properties`:

```properties
# EPCIS WebSocket server URL
quarkus.rest-client.epcis-api.url=https://your-epcis-server.com/epcis/v2

# Authentication (if required)
openepcis-client.api-key=your-api-key
openepcis-client.api-key-secret=your-api-secret
```

As an alternative you can export the environment variables directly in your terminal:  
```sh
export OPENEPCIS_CLIENT_API_KEY=<your-api-key>
export OPENEPCIS_CLIENT_API_KEY_SECRET=<your-api-key-secret>
export QUARKUS_REST_CLIENT__EPCIS_API__URL=https://custom-epcis-api-url.com
```

## Building Native Images

This application can be compiled into a native executable using Quarkus:

```bash
mvn package -Pnative
```

The native image will be available at `target/openepcis-subscription-example-runner`.

To run the native image:

```bash
./target/openepcis-subscription-example-runner
```

### Docker Build (Optional)

If you have Docker installed, you can build a native image using a Docker container:

```bash
mvn package -Pnative -Dquarkus.native.container-build=true
```

## License

This project is licensed under the [Apache License 2.0](https://www.apache.org/licenses/LICENSE-2.0).
