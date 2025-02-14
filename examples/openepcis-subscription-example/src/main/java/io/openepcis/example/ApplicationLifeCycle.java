package io.openepcis.example;

import io.openepcis.client.websocket.WebSocketClient;
import io.quarkus.runtime.ShutdownEvent;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.websocket.Session;
import org.jboss.logmanager.Logger;

import java.io.IOException;

/**
 * ApplicationLifeCycle is a lifecycle management bean for a Quarkus application.
 * It observes the application startup and shutdown events to manage the WebSocket connection for EPCIS event processing.
 * <p>
 * On startup, it establishes a WebSocket session to subscribe to a specific EPCIS query.
 * On shutdown, it gracefully closes the WebSocket session.
 */
@ApplicationScoped
public class ApplicationLifeCycle {

  private static final Logger LOGGER = Logger.getLogger("ApplicationLifeCycle");

  private final EPCISQueryResultProcessor epcisQueryResultProcessor;
  private final WebSocketClient webSocketClient;
  private Session session;  // The WebSocket session for the EPCIS query subscription

  /**
   * Constructor for ApplicationLifeCycle.
   *
   * @param epcisQueryResultProcessor The processor for handling EPCIS query results.
   * @param webSocketClient           The client for establishing WebSocket connections to EPCIS queries.
   */
  public ApplicationLifeCycle(final EPCISQueryResultProcessor epcisQueryResultProcessor, final WebSocketClient webSocketClient) {
    this.epcisQueryResultProcessor = epcisQueryResultProcessor;
    this.webSocketClient = webSocketClient;
  }

  /**
   * Observes the startup event of the application.
   * <p>
   * This method is triggered when the application starts. It establishes a WebSocket connection to subscribe to an EPCIS query
   * named "shippingSample01". The query results are processed by the provided {@link EPCISQueryResultProcessor}.
   *
   * @param ev The {@link StartupEvent} representing the application startup event.
   */
  void onStart(@Observes StartupEvent ev) {
    LOGGER.info("The application is starting...");

    webSocketClient.querySubscription("shippingSample01", epcisQueryResultProcessor::process)
            .subscribe().with(session -> {
              if (session.isOpen()) {
                LOGGER.info("WebSocket session opened");
              } else {
                LOGGER.warning("WebSocket session is closed");
              }
              this.session = session;
            });
  }

  /**
   * Observes the shutdown event of the application.
   * <p>
   * This method is triggered when the application is shutting down. It closes the WebSocket session if it is open to ensure
   * a clean shutdown.
   *
   * @param ev The {@link ShutdownEvent} representing the application shutdown event.
   * @throws IOException If an error occurs while closing the WebSocket session.
   */
  void onStop(@Observes ShutdownEvent ev) throws IOException {
    LOGGER.info("The application is stopping...");
    if (this.session != null) {
      this.session.close();
      LOGGER.info("WebSocket session closed");
    }
  }
}
