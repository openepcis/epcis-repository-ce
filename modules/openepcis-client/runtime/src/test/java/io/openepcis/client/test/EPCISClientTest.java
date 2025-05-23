package io.openepcis.client.test;

import io.openepcis.client.rest.LowLevelEPCISClient;
import io.openepcis.model.epcis.EPCISQueryDocument;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.MultivaluedHashMap;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.List;

@QuarkusTest
public class EPCISClientTest {

  @Inject
  LowLevelEPCISClient client;

  @Test
  @Disabled
  void runTest() throws Exception {
    Assertions.assertNotNull(client.events());
    Assertions.assertNotNull(client.capture());
    MultivaluedHashMap<String, String> q = new MultivaluedHashMap();
    q.put("eventType", List.of("ObjectEvent"));
    Assertions.assertNotNull(client.events().eventsGet(q).map(r -> r.readEntity(EPCISQueryDocument.class)).await().atMost(Duration.of(10, ChronoUnit.SECONDS)));
  }

}
