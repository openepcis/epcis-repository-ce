package io.openepcis.client.test;

import io.openepcis.client.rest.ClientConfig;
import io.openepcis.client.websocket.WebSocketClient;
import io.quarkus.logging.Log;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

@QuarkusTest
public class WebSocketClientTest {

  @Inject
  WebSocketClient client;

  @Inject
  ClientConfig config;

  @Test
  @Disabled
  void runTest() throws Exception {
    Assertions.assertNotNull(client.querySubscription("shippingSample01", s -> {
      Log.debug("msg="+s);
    }).await().atMost(Duration.of(10, ChronoUnit.SECONDS)));
  }

}
