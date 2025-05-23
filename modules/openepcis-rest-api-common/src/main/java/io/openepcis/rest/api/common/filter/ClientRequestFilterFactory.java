package io.openepcis.rest.api.common.filter;

import jakarta.ws.rs.client.ClientRequestFilter;

public interface ClientRequestFilterFactory {

  ClientRequestFilter newClientRequestFilter();

}
