Here’s a short README for the **openepcis-client** project with the required environment variables and brief explanations.

---

# OpenEPCIS Client

The **OpenEPCIS Client** is a lightweight, scalable client for interacting with EPCIS repositories. It supports both REST-based EPCIS queries and real-time subscription using WebSocket for event streams.

## Features
- **REST Client** for EPCIS 2.0 Queries
- **WebSocket Client** for EPCIS WebSocket Query Subscription Support
- **Authentication** using API Key and Secret
- Configurable and extensible for different EPCIS query requirements

## Configuration

Ensure the following environment variables are set before running the client:

| Environment Variable                  | Description                          | Default Value                |
|----------------------------------------|--------------------------------------|-----------------------------|
| `OPENEPCIS_CLIENT_API_KEY`             | API Key for authenticating requests  | (required)                  |
| `OPENEPCIS_CLIENT_API_KEY_SECRET`      | API Key Secret for authentication    | (required)                  |
| `QUARKUS_REST_CLIENT__EPCIS_API__URL`  | Base URL of the EPCIS API            | `https://api.epcis.cloud`   |

## Usage

1. **Set the environment variables**:  
   You can export the environment variables directly in your terminal.  
   Example:
   ```sh
   export OPENEPCIS_CLIENT_API_KEY=<your-api-key>
   export OPENEPCIS_CLIENT_API_KEY_SECRET=<your-api-key-secret>
   export QUARKUS_REST_CLIENT__EPCIS_API__URL=https://custom-epcis-api-url.com
   ```
