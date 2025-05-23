# Distributions

This folder defines Quarkus-based packaging for the main REST API editions of OpenEPCIS.

## Subdirectories

- **community-edition/**  
  Stable, open-source implementation of the EPCIS 2.0 REST API. Use this for production-grade testing and community deployments.

- **research-edition/**  
  Build with extended features, such as the SAX-based EPCIS XML-to-JSON converter. ⚠️ Requires separate license for production use.

Each edition includes:
- Quarkus configuration files
- `pom.xml` for module assembly
