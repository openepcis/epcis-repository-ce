package io.openepcis.client.rest;

import io.openepcis.rest.api.common.filter.ClientRequestFilterFactory;
import jakarta.enterprise.inject.spi.CDI;
import jakarta.ws.rs.client.ClientRequestFilter;

/**
 * EPCISClientRequestFilterFactory is responsible for creating instances of {@link EPCISClientRequestFilter}.
 * This factory utilizes CDI (Contexts and Dependency Injection) to retrieve an instance of {@link EPCISClientRequestFilterSupport},
 * which provides the actual filter instance.
 */
public class EPCISClientRequestFilterFactory implements ClientRequestFilterFactory {

  /**
   * Creates a new {@link ClientRequestFilter} using {@link EPCISClientRequestFilterSupport}.
   *
   * @return A new instance of {@link EPCISClientRequestFilter}.
   * @throws RuntimeException if {@link EPCISClientRequestFilterSupport} is not available via CDI.
   */
  @Override
  public ClientRequestFilter newClientRequestFilter() {
    // Check if EPCISClientRequestFilterSupport is available through CDI
    if (CDI.current().select(EPCISClientRequestFilterSupport.class).isUnsatisfied()) {
      throw new RuntimeException("EPCISClientRequestFilterSupport is unsatisfied");
    }

    // Retrieve and return a new EPCISClientRequestFilter instance
    return CDI.current().select(EPCISClientRequestFilterSupport.class).get().newEPCISClientRequestFilter();
  }
}
