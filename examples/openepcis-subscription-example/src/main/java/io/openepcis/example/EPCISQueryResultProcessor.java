package io.openepcis.example;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.openepcis.client.rest.ReactiveEPCISClient;
import io.openepcis.model.epcis.AggregationEvent;
import io.openepcis.model.epcis.EpcisQueryResult;
import io.openepcis.model.epcis.ObjectEvent;
import io.openepcis.model.epcis.QuantityList;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.MultivaluedHashMap;
import org.jboss.logmanager.Logger;

import java.time.format.DateTimeFormatter;
import java.util.stream.Stream;

/**
 * EPCISQueryResultProcessor processes EPCIS query results received from the subscription.
 * It identifies {@link ObjectEvent} instances and queries for related {@link AggregationEvent} records
 * based on the parentID found in the ObjectEvent.
 * <p>
 * The class uses {@link ReactiveEPCISClient} to perform reactive queries and logs details about child EPCs.
 */
@ApplicationScoped
public class EPCISQueryResultProcessor {

  private static final Logger LOGGER = Logger.getLogger("EPCISQueryResultProcessor");

  private final ReactiveEPCISClient client;
  private final ObjectMapper objectMapper;

  /**
   * Constructor for EPCISQueryResultProcessor.
   *
   * @param client       The reactive EPCIS client used for querying EPCIS events.
   * @param objectMapper The Jackson {@link ObjectMapper} for handling JSON data.
   */
  public EPCISQueryResultProcessor(final ReactiveEPCISClient client, final ObjectMapper objectMapper) {
    this.client = client;
    this.objectMapper = objectMapper;
  }

  /**
   * Processes the provided {@link EpcisQueryResult} by:
   * <ul>
   *   <li>Logging the number of events received from the subscription.</li>
   *   <li>Iterating through each {@link ObjectEvent} to retrieve related {@link AggregationEvent} instances
   *       where the `MATCH_parentID` corresponds to an EPC in the ObjectEvent.</li>
   *   <li>Logging the child EPCs and child quantity EPC classes from the related AggregationEvents.</li>
   * </ul>
   *
   * @param epcisQueryResult The query result to process.
   */
  public void process(final EpcisQueryResult epcisQueryResult) {
    LOGGER.info("got " + epcisQueryResult.getResultsBody().getEventList().size() + " event(s) from subscription");

    // Iterate over the list of EPCIS events in the query result
    epcisQueryResult.getResultsBody().getEventList().forEach(epcisEvent -> {
      if (epcisEvent instanceof ObjectEvent objectEvent) {  // Check if the event is an ObjectEvent
        objectEvent.getEpcList().forEach(epc -> {
          LOGGER.info("getting AggregationEvents with parentID for " + epc);

          // Build a query to find related AggregationEvents with the same parentID
          MultivaluedHashMap<String, String> query = new MultivaluedHashMap<>();
          query.add("eventType", "AggregationEvent");
          query.add("EQ_action", "ADD");
          query.add("LT_eventTime", objectEvent.getEventTime().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
          query.add("MATCH_parentID", epc);

          // Execute the query and process the result
          client.events(b -> b.withQuery(query)).subscribe().with(queryResult -> {
            LOGGER.info("childEPCs: " + queryResult.getEpcisBody().getQueryResults()
                    .getResultsBody().getEventList().stream().flatMap(event -> {
                      AggregationEvent aggregationEvent = (AggregationEvent) event;

                      // Extract child EPCs or child quantity EPC classes from the AggregationEvent
                      if (aggregationEvent.getChildEPCs() != null) {
                        return aggregationEvent.getChildEPCs().stream();
                      }
                      if (aggregationEvent.getChildQuantityList() != null) {
                        return aggregationEvent.getChildQuantityList().stream().map(QuantityList::getEpcClass);
                      }
                      return Stream.of();
                    }).toList());
          });
        });
      }
    });
  }
}
