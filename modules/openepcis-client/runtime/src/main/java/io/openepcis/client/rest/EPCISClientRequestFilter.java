package io.openepcis.client.rest;

import jakarta.ws.rs.client.ClientRequestContext;
import jakarta.ws.rs.client.ClientRequestFilter;
import jakarta.ws.rs.core.HttpHeaders;

import java.io.IOException;
import java.util.Base64;

/**
 * EPCISClientRequestFilter is a request filter for adding authentication headers to outbound HTTP requests.
 * It supports two authentication mechanisms:
 * 1. API Key-based authentication.
 * 2. Basic authentication with username and password.
 */
public class EPCISClientRequestFilter implements ClientRequestFilter {

  private final ClientConfig config;

  /**
   * Constructor for EPCISClientRequestFilter.
   *
   * @param config The {@link ClientConfig} containing API key, secret, username, and password configurations.
   */
  public EPCISClientRequestFilter(final ClientConfig config) {
    this.config = config;
  }

  /**
   * Adds the appropriate authentication headers to each outgoing request based on the available configuration.
   * - If API Key and API Key Secret are present, it adds them as headers.
   * - If username and password are present, it adds a Basic Authorization header.
   *
   * @param requestContext The context of the HTTP request being processed.
   * @throws IOException If an I/O error occurs.
   */
  @Override
  public void filter(ClientRequestContext requestContext) throws IOException {
    // Check for API Key and API Key Secret
    if (config.apiKeySecret().isPresent() && config.apiKey().isPresent()) {
      requestContext.getHeaders().add("API-KEY", config.apiKey().get());
      requestContext.getHeaders().add("API-KEY-SECRET", config.apiKeySecret().get());
    }
    // Check for username and password for Basic Authentication
    else if (config.username().isPresent() && config.password().isPresent()) {
      final String credentials = config.username().get() + ":" + config.password().get();
      final String encoding = Base64.getEncoder().encodeToString(credentials.getBytes());
      requestContext.getHeaders().add(HttpHeaders.AUTHORIZATION, "Basic " + encoding);
    }
  }
}
