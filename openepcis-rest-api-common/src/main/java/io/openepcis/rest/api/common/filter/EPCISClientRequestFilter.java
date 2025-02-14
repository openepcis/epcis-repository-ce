package io.openepcis.rest.api.common.filter;

import jakarta.ws.rs.client.ClientRequestContext;
import jakarta.ws.rs.client.ClientRequestFilter;
import jakarta.ws.rs.ext.Provider;

import java.io.IOException;
import java.util.List;
import java.util.ServiceLoader;

@Provider
public class EPCISClientRequestFilter implements ClientRequestFilter {

  private final List<ClientRequestFilter> clientRequestFilters;

  public EPCISClientRequestFilter() {
    this.clientRequestFilters = ServiceLoader.load(ClientRequestFilterFactory.class).stream().map(f -> f.get().newClientRequestFilter()).toList();
  }

  @Override
  public void filter(ClientRequestContext requestContext) throws IOException {
    for (final ClientRequestFilter f : clientRequestFilters) {
      f.filter(requestContext);
    }
  }
}
