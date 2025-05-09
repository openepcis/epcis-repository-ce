package io.openepcis.client.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.openepcis.model.epcis.EPCISQueryDocument;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.subscription.MultiEmitter;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;

import java.io.InputStream;
import java.util.function.Consumer;

/**
 * ReactiveEPCISClient is a reactive client for querying EPCIS events using a reactive programming model.
 * It provides a continuous stream of {@link EPCISQueryDocument} results and handles paginated responses
 * by recursively following next page links.
 */
@ApplicationScoped
public class ReactiveEPCISClient {

  @Inject
  LowLevelEPCISClient client;  // The low-level client that performs the actual HTTP requests to the EPCIS server

  @Inject
  ObjectMapper objectMapper;  // Used for mapping JSON responses to EPCISQueryDocument objects


  /**
   * Creates a reactive stream of {@link EPCISQueryDocument} based on the query built using the provided builder.
   *
   * @param builder A {@link Consumer} that customizes the query using {@link EventsQueryBuilder}.
   * @return A {@link Multi} that emits {@link EPCISQueryDocument} instances.
   */
  public Multi<EPCISQueryDocument> events(Consumer<EventsQueryBuilder> builder) {
    final EventsQueryBuilder b = new EventsQueryBuilder();
    builder.accept(b);
    return Multi.createFrom().emitter(em -> executeEventsQuery(em, b));
  }

  /**
   * Executes the initial events query and emits the results through the provided {@link MultiEmitter}.
   * If the response contains a "Link" header, it follows the link to fetch the next set of results recursively.
   *
   * @param em The emitter to send the results to the reactive stream.
   * @param b  The query builder containing the query parameters and headers.
   */
  private void executeEventsQuery(MultiEmitter<? super EPCISQueryDocument> em, EventsQueryBuilder b) {
    client.events().eventsGet(b.query(), b.headers())
            .subscribe().with(response -> {
              if (response.getStatus() == 200) {
                try {
                  // Deserialize the response body into an EPCISQueryDocument
                  final EPCISQueryDocument queryResult = response.readEntity(EPCISQueryDocument.class);
                  em.emit(queryResult);

                  // Check if there is a next page link in the response
                  final String nextLink = response.getHeaderString("Link");
                  if (nextLink == null) {
                    em.complete();
                  } else {
                    recurseNextLink(em, nextLink);
                  }
                } catch (Exception e) {
                  em.fail(e);
                }
              }
            }, em::fail);  // Fail the emitter if an error occurs
  }

  /**
   * Recursively follows the "Link" header to fetch the next set of results.
   *
   * @param em           The emitter to send the results to the reactive stream.
   * @param nextLinkParam The "Link" header containing the URL for the next page.
   */
  private void recurseNextLink(MultiEmitter<? super EPCISQueryDocument> em, String nextLinkParam) {
    // Extract the next page token from the "Link" header
    final String[] nextPageTokenParameter = nextLinkParam
            .substring(nextLinkParam.indexOf("<") + 1, nextLinkParam.indexOf(">"))
            .replaceFirst("/events\\?", "")
            .split("=");

    // Build a new query with the next page token
    final EventsQueryBuilder b = new EventsQueryBuilder();
    b.query().add(nextPageTokenParameter[0], nextPageTokenParameter[1]);

    // Execute the next query recursively
    executeEventsQuery(em, b);
  }

  /**
   * A builder class for constructing query parameters and headers for the EPCIS events query.
   */
  public static final class EventsQueryBuilder {
    private MultivaluedMap<String, String> query;
    private MultivaluedMap<String, String> headers;

    /**
     * Sets the query parameters.
     *
     * @param query A {@link MultivaluedMap} containing the query parameters.
     * @return The current {@link EventsQueryBuilder} instance.
     */
    public EventsQueryBuilder withQuery(MultivaluedMap<String, String> query) {
      this.query = query;
      return this;
    }

    /**
     * Sets the request headers.
     *
     * @param headers A {@link MultivaluedMap} containing the request headers.
     * @return The current {@link EventsQueryBuilder} instance.
     */
    public EventsQueryBuilder withHeaders(MultivaluedMap<String, String> headers) {
      this.headers = headers;
      return this;
    }

    /**
     * Retrieves or initializes the query parameters.
     *
     * @return A {@link MultivaluedMap} containing the query parameters.
     */
    public MultivaluedMap<String, String> query() {
      if (query == null) {
        query = new MultivaluedHashMap<>();
      }
      return query;
    }

    /**
     * Retrieves or initializes the request headers.
     *
     * @return A {@link MultivaluedMap} containing the request headers.
     */
    public MultivaluedMap<String, String> headers() {
      if (headers == null) {
        headers = new MultivaluedHashMap<>();
      }
      return headers;
    }
  }
}
