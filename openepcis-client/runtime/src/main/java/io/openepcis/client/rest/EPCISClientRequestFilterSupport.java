package io.openepcis.client.rest;

import io.quarkus.runtime.Startup;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

/**
 * EPCISClientRequestFilterSupport acts as a factory for creating instances of {@link EPCISClientRequestFilter}.
 * This class ensures that each instance of the request filter is initialized with the current {@link ClientConfig}.
 */
@Startup
@ApplicationScoped
public class EPCISClientRequestFilterSupport {

  // Injects the ClientConfig to be used for creating the EPCISClientRequestFilter
  @Inject
  ClientConfig config;

  /**
   * Creates a new instance of {@link EPCISClientRequestFilter} with the injected configuration.
   *
   * @return A new EPCISClientRequestFilter instance configured with the current {@link ClientConfig}.
   */
  public EPCISClientRequestFilter newEPCISClientRequestFilter() {
    return new EPCISClientRequestFilter(config);
  }
}
