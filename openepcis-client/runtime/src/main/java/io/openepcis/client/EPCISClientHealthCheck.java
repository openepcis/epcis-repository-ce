package io.openepcis.client;

import io.openepcis.client.rest.LowLevelEPCISClient;
import io.openepcis.client.websocket.WebSocketClient;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.HealthCheckResponseBuilder;
import org.eclipse.microprofile.health.Readiness;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

/**
 * EPCISClientHealthCheck implements a readiness health check for the OpenEPCIS client.
 * This health check verifies if the REST client for EPCIS events is available and responsive within a specified timeout.
 * <p>
 * It uses MicroProfile Health to provide a readiness check, ensuring that the EPCIS client is ready for handling requests.
 */
@Readiness
@ApplicationScoped
public class EPCISClientHealthCheck implements HealthCheck {

  private final LowLevelEPCISClient client;
  private final WebSocketClient wsClient;

  /**
   * Constructor for EPCISClientHealthCheck.
   *
   * @param client   The {@link LowLevelEPCISClient} used for interacting with the EPCIS REST API.
   * @param wsClient The {@link WebSocketClient} used for WebSocket communication (not currently checked in this implementation).
   */
  public EPCISClientHealthCheck(final LowLevelEPCISClient client, final WebSocketClient wsClient) {
    this.client = client;
    this.wsClient = wsClient;
  }

  /**
   * Executes the health check by calling the EPCIS client to verify connectivity and readiness.
   *
   * @return A {@link HealthCheckResponse} indicating whether the EPCIS client is healthy or not.
   */
  @Override
  public HealthCheckResponse call() {
    HealthCheckResponseBuilder builder =
            HealthCheckResponse.named("OpenEPCIS EPCIS client health check").up();

    try {
      // Try to fetch event options from the EPCIS client with a 5-second timeout
      builder.withData(
              "options",
              client.events()
                      .getEventsOptions()
                      .await()
                      .atMost(Duration.of(5, ChronoUnit.SECONDS))
                      .getHeaders()
                      .toString()
      );
    } catch (Exception e) {
      // If an exception occurs, mark the health check as down and include the error message
      builder.withData("error", e.getMessage());
      builder.down();
    }

    // Return the final health check response
    return builder.build();
  }
}
