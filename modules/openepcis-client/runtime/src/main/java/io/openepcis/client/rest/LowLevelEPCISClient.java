package io.openepcis.client.rest;

import io.openepcis.rest.api.common.CaptureApi;
import io.openepcis.rest.api.common.EventsApi;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.rest.client.inject.RestClient;

/**
 * LowLevelEPCISClient serves as a central client for interacting with the EPCIS REST APIs.
 * It provides access to two main services:
 * 1. {@link EventsApi} for querying and retrieving EPCIS events.
 * 2. {@link CaptureApi} for capturing and submitting EPCIS events.
 */
@ApplicationScoped
public class LowLevelEPCISClient {

  // Injects the EventsApi REST client for querying EPCIS events
  @RestClient
  EventsApi eventsApi;

  // Injects the CaptureApi REST client for capturing EPCIS events
  @RestClient
  CaptureApi captureApi;

  /**
   * Provides access to the Events API client for querying EPCIS events.
   *
   * @return An instance of {@link EventsApi} for querying events.
   */
  public EventsApi events() {
    return eventsApi;
  }

  /**
   * Provides access to the Capture API client for capturing EPCIS events.
   *
   * @return An instance of {@link CaptureApi} for submitting events.
   */
  public CaptureApi capture() {
    return captureApi;
  }

}
