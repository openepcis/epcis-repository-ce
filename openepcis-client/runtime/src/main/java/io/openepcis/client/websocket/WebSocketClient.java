package io.openepcis.client.websocket;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.openepcis.client.rest.ClientConfig;
import io.openepcis.model.epcis.EpcisQueryResult;
import io.quarkus.logging.Log;
import io.quarkus.runtime.Startup;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.websocket.*;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * WebSocketClient establishes a WebSocket connection to an EPCIS server for real-time event streaming.
 * This client is configured to connect to a specific query endpoint and process incoming messages using a callback function.
 */
@Startup
@ApplicationScoped
public class WebSocketClient {

  // Injected configuration for client settings such as API keys
  @Inject
  ClientConfig config;

  @Inject
  ObjectMapper objectMapper;

  // URL for the EPCIS API, read from the application configuration
  @ConfigProperty(name = "quarkus.rest-client.epcis-api.url")
  String apiUrl;

  /**
   * Establishes a WebSocket connection to the specified query subscription and listens for events.
   *
   * @param queryName The name of the EPCIS query to subscribe to.
   * @param callback  A function that processes each message received from the WebSocket.
   * @return A Uni containing the WebSocket session or an error if the connection fails.
   */
  public Uni<Session> querySubscription(final String queryName, final Consumer<EpcisQueryResult> callback) {
    // Build the WebSocket client configuration
    final ClientEndpointConfig.Builder configBuilder = ClientEndpointConfig.Builder.create();

    // Set custom headers for API authentication
    configBuilder.configurator(new ClientEndpointConfig.Configurator() {
      @Override
      public void beforeRequest(Map<String, List<String>> headers) {
        headers.put("API-KEY", List.of(config.apiKey().get()));
        headers.put("API-KEY-SECRET", List.of(config.apiKeySecret().get()));
      }
    });

    try {
      // Convert the HTTP API URL to a WebSocket URL for subscribing to events
      final String endpointUrl = apiUrl
              .replaceFirst("http", "ws")    // Replace 'http' with 'ws' for WebSocket protocol
              .concat("/queries/")
              .concat(queryName)
              .concat("/events?stream=true");  // Append the query path and enable streaming

      // Create a new WebSocket client endpoint with the provided callback
      final ClientEndpoint endpoint = new ClientEndpoint(objectMapper, callback);

      // Connect to the WebSocket server and return the session as a Uni
      final Session session = jakarta.websocket.ContainerProvider.getWebSocketContainer()
              .connectToServer(endpoint, configBuilder.build(), URI.create(endpointUrl));
      return Uni.createFrom().item(session);
    } catch (Exception e) {
      // Return a Uni with the failure if an exception occurs during connection
      return Uni.createFrom().failure(e);
    }
  }

  /**
   * ClientEndpoint is a custom WebSocket endpoint that handles incoming messages and passes them to a callback function.
   */
  public static class ClientEndpoint extends Endpoint {

    private final ObjectMapper objectMapper;
    private final Consumer<EpcisQueryResult> callback;

    /**
     * Constructs a ClientEndpoint with the given callback function.
     *
     * @param callback A function that processes messages received from the WebSocket.
     */
    public ClientEndpoint(ObjectMapper objectMapper, final Consumer<EpcisQueryResult> callback) {
      this.objectMapper = objectMapper;
      this.callback = callback;
    }

    /**
     * Called when the WebSocket connection is opened. Registers a message handler for incoming messages.
     *
     * @param session       The WebSocket session.
     * @param endpointConfig The endpoint configuration.
     */
    @Override
    public void onOpen(Session session, EndpointConfig endpointConfig) {
      // Send an initial message to indicate readiness
      session.getAsyncRemote().sendText("_ready_");

      // Register a message handler to process incoming messages
      session.addMessageHandler(new MessageHandler.Whole<String>() {
        @Override
        public void onMessage(String message) {
          final EpcisQueryResult epcisQueryResult;
          try {
            epcisQueryResult = objectMapper.readValue(message, EpcisQueryResult.class);
            // Pass the received message to the callback function
            callback.accept(epcisQueryResult);
          } catch (JsonProcessingException e) {
            Log.error(e.getMessage(), e);
          }
        }
      });
    }
  }
}
