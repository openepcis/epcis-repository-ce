package io.openepcis.client.test;

import io.openepcis.client.rest.ReactiveEPCISClient;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.MultivaluedHashMap;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.List;

@QuarkusTest
public class ReactiveEPCISClientTest {

  @Inject
  ReactiveEPCISClient client;

  @Test
  @Disabled
  void runTest() throws Exception {
    MultivaluedHashMap<String, String> query = new MultivaluedHashMap();
    query.put("eventType", List.of("ObjectEvent"));
    query.put("perPage", List.of("1"));
    client.events(b -> b.withQuery(query)).collect().asList().await().atMost(Duration.of(10, ChronoUnit.SECONDS));
  }

}
