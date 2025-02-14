/*
 * Copyright 2022-2024 benelog GmbH & Co. KG
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 */
package io.openepcis.rest.api.common;

import io.openepcis.model.epcis.format.CBVFormat;
import io.openepcis.model.epcis.format.EPCFormat;
import io.openepcis.model.rest.EPCISQuery;
import io.openepcis.model.rest.ProblemResponseBody;
import io.openepcis.rest.api.common.constants.ParameterDescriptions;
import io.openepcis.rest.api.common.constants.ResponseBodyExamples;
import io.openepcis.rest.api.common.filter.EPCISClientRequestFilter;
import io.quarkus.security.identity.SecurityIdentity;
import io.smallrye.mutiny.Uni;
import io.vertx.core.http.HttpServerRequest;
import jakarta.annotation.security.RolesAllowed;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.ParameterIn;
import org.eclipse.microprofile.openapi.annotations.headers.Header;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameters;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.openapi.annotations.tags.Tags;
import org.eclipse.microprofile.rest.client.annotation.RegisterProvider;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import org.jboss.resteasy.reactive.RestHeader;
import org.jboss.resteasy.reactive.RestPath;
import org.jboss.resteasy.reactive.RestQuery;

import static io.openepcis.rest.api.common.constants.ParameterConstants.*;

@Tag(
        name = "Queries",
        description =
                "Endpoints to create large named or anonymous queries using the EPCIS Query Language. Named queries have a custom\n"
                        + "      name and are stored until deleted by the user. Anonymous queries are not persisted and only available to the caller.\n"
                        + "      EPCIS events queries also support query subscription.")
@Path("queries")
@RegisterRestClient(configKey = "epcis-api")
@RegisterProvider(EPCISClientRequestFilter.class)
public interface QueryApi {

    @Operation(
            description = "An endpoint to list named queries. This endpoint supports pagination.",
            summary = "Returns a list of queries available")
    @Parameters(
            value = {
                    @Parameter(
                            name = "nextPageToken",
                            description = ParameterDescriptions.NEXT_PAGE_TOKEN,
                            in = ParameterIn.QUERY,
                            content = @Content(example = "3A15506738749783AU6D7DENAKwM2gQRRwGritaeq")),
                    @Parameter(
                            name = "perPage",
                            description = ParameterDescriptions.PER_PAGE,
                            in = ParameterIn.QUERY),
                    @Parameter(
                            name = "GS1-CBV-Min",
                            description = ParameterDescriptions.GS1_CBV_MIN,
                            in = ParameterIn.HEADER,
                            content = @Content(example = DEFAULT_CBV_MIN_PARAMETER_VALUE)),
                    @Parameter(
                            name = "GS1-CBV-Max",
                            description = ParameterDescriptions.GS1_CBV_MAX,
                            in = ParameterIn.HEADER,
                            content = @Content(example = DEFAULT_CBV_MAX_PARAMETER_VALUE)),
                    @Parameter(
                            name = "GS1-EPCIS-Min",
                            description = ParameterDescriptions.GS1_EPCIS_MIN,
                            in = ParameterIn.HEADER,
                            content = @Content(example = DEFAULT_EPCIS_MIN_PARAMETER_VALUE)),
                    @Parameter(
                            name = "GS1-EPCIS-Max",
                            description = ParameterDescriptions.GS1_EPCIS_MAX,
                            in = ParameterIn.HEADER,
                            content = @Content(example = DEFAULT_EPCIS_MAX_PARAMETER_VALUE)),
                    @Parameter(
                            name = "GS1-Extensions",
                            description = ParameterDescriptions.GS1_EXTENSIONS,
                            in = ParameterIn.HEADER)
            }
    )
    @APIResponses(
            value = {
                    @APIResponse(
                            responseCode = "200",
                            description =
                                    "Queries are like views that are created using the <a href=\"https://github.com/gs1/EPCIS/tree/master/REST%20Bindings/query-schema.json\">EPCIS Query Language</a>. Each query object"
                                            + "            consists of a query name and the query definition. "
                                            + "            An EPCIS 2.0 query body using the REST interface SHALL be serialised as a JSON object. The value of the query key within that JSON object SHALL validate against the schema defined at:  https://ref.gs1.org/standards/epcis/2.0.0/query-schema.json.\n"
                                            + "            Performing a `GET` on `/queries` lists all existing queries.",
                            headers = {
                                    @Header(
                                            name = "GS1-EPCIS-version",
                                            description = ParameterDescriptions.GS1_EPCIS_VERSION,
                                            schema = @Schema(implementation = String.class)),
                                    @Header(
                                            name = "GS1-CBV-version",
                                            description = ParameterDescriptions.GS1_CBV_VERSION,
                                            schema = @Schema(implementation = String.class)),
                                    @Header(
                                            name = "GS1-Extensions",
                                            description = ParameterDescriptions.GS1_EXTENSIONS,
                                            schema = @Schema(implementation = String.class)),
                                    @Header(
                                            name = "Link",
                                            description = ParameterDescriptions.LINK,
                                            schema = @Schema(implementation = String.class)),
                                    @Header(
                                            name = "GS1-Next-Page-Token-Expires",
                                            description = ParameterDescriptions.GS1_NEXT_PAGE_TOKEN_EXPIRES,
                                            schema = @Schema(implementation = String.class))
                            },
                            content =
                            @Content(
                                    example =
                                            "[{\n"
                                                    + "  \"query\": {\n"
                                                    + "  \"EQ_bizStep\": [\n"
                                                    + "      \"shipping\",\n"
                                                    + "      \"receiving\"\n"
                                                    + "  ],\n"
                                                    + "  \"eventType\": [\"ObjectEvent\"]\n"
                                                    + "},\n"
                                                    + "\"name\": \"myQuery\"\n"
                                                    + "}]")),
                    @APIResponse(
                            responseCode = "401",
                            description = "Authorization information is missing or invalid.",
                            content =
                            @Content(
                                    schema = @Schema(implementation = ProblemResponseBody.class),
                                    example = ResponseBodyExamples.RESPONSE_401_UNAUTHORIZED_REQUEST)),
                    @APIResponse(
                            responseCode = "403",
                            description = "Client is unauthorized to access this resource",
                            content =
                            @Content(
                                    schema = @Schema(implementation = ProblemResponseBody.class),
                                    example = ResponseBodyExamples.RESPONSE_403_CLIENT_UNAUTHORIZED)),
                    @APIResponse(
                            responseCode = "406",
                            description = "The server cannot return the response as requested.",
                            content =
                            @Content(
                                    schema = @Schema(implementation = ProblemResponseBody.class),
                                    example = ResponseBodyExamples.RESPONSE_406_NOT_ACCEPTABLE)),
                    @APIResponse(
                            responseCode = "500",
                            description = "This is a server-side problem.",
                            content =
                            @Content(
                                    schema = @Schema(implementation = ProblemResponseBody.class),
                                    example = ResponseBodyExamples.RESPONSE_500_IMPLEMENTATION_EXCEPTION))
            })
    @GET
    @Produces({MediaType.APPLICATION_JSON, "application/problem+json", "application/ld+json"})
    @Consumes({MediaType.APPLICATION_JSON, "application/ld+json"})
    public Uni<Response> queriesGet(
            @Valid
            @RestQuery
            String nextPageToken,
            @Valid
            @RestQuery(value = "perPage")
            @DefaultValue(DEFAULT_PER_PAGE_PARAMETER_VALUE)
            Integer perPage);

    @Operation(
            description =
                    "Creating a named query creates a view on the events in the repository, accessible through its events resource."
                            + "To obtain the named query results, the client can use the URL in the `Location` header. The client can also use this URL to start a query subscription immediately after creating the query.",
            summary = "Creates a named EPCIS events query")
    @Parameters(
            value = {
                    @Parameter(
                            name = "GS1-Extensions",
                            description = ParameterDescriptions.GS1_EXTENSIONS,
                            in = ParameterIn.HEADER),
                    @Parameter(
                            name = "GS1-EPCIS-Version",
                            description = ParameterDescriptions.GS1_EPCIS_VERSION,
                            in = ParameterIn.HEADER),
                    @Parameter(
                            name = "GS1-CBV-Version",
                            description = ParameterDescriptions.GS1_EPCIS_VERSION,
                            in = ParameterIn.HEADER)
            }
    )
    @APIResponses(
            value = {
                    @APIResponse(
                            responseCode = "201",
                            description = "Creates the named query.",
                            headers = {
                                    @Header(
                                            name = "GS1-EPCIS-version",
                                            description = ParameterDescriptions.GS1_EPCIS_VERSION,
                                            schema = @Schema(implementation = String.class)),
                                    @Header(
                                            name = "GS1-CBV-version",
                                            description = ParameterDescriptions.GS1_CBV_VERSION,
                                            schema = @Schema(implementation = String.class)),
                                    @Header(
                                            name = "GS1-Extensions",
                                            description = ParameterDescriptions.GS1_EXTENSIONS,
                                            schema = @Schema(implementation = String.class)),
                                    @Header(
                                            name = "Location",
                                            description = ParameterDescriptions.LOCATION_QUERY,
                                            schema = @Schema(implementation = String.class))
                            }),
                    @APIResponse(
                            responseCode = "401",
                            description = "Authorization information is missing or invalid.",
                            content =
                            @Content(
                                    schema = @Schema(implementation = ProblemResponseBody.class),
                                    example = ResponseBodyExamples.RESPONSE_401_UNAUTHORIZED_REQUEST)),
                    @APIResponse(
                            responseCode = "403",
                            description = "Client is unauthorized to access this resource",
                            content =
                            @Content(
                                    schema = @Schema(implementation = ProblemResponseBody.class),
                                    example = ResponseBodyExamples.RESPONSE_403_CLIENT_UNAUTHORIZED)),
                    @APIResponse(
                            responseCode = "406",
                            description = "The server cannot return the response as requested.",
                            content =
                            @Content(
                                    schema = @Schema(implementation = ProblemResponseBody.class),
                                    example = ResponseBodyExamples.RESPONSE_406_NOT_ACCEPTABLE)),
                    @APIResponse(
                            responseCode = "409",
                            description = "A named query with the provided identifier already exists.",
                            content =
                            @Content(
                                    schema = @Schema(implementation = ProblemResponseBody.class),
                                    example = ResponseBodyExamples.RESPONSE_409_RESOURCE_ALREADY_EXISTS_EXCEPTION)),
                    @APIResponse(
                            responseCode = "415",
                            description = "The client sent data in a format that is not supported by the server.",
                            content =
                            @Content(
                                    schema = @Schema(implementation = ProblemResponseBody.class),
                                    example = ResponseBodyExamples.RESPONSE_415_UNSUPPORTED_MEDIA_TYPE)),
                    @APIResponse(
                            responseCode = "500",
                            description =
                                    "This is a server-side problem caused when the query was resolved and executed.",
                            content =
                            @Content(
                                    schema = @Schema(implementation = ProblemResponseBody.class),
                                    example = ResponseBodyExamples.RESPONSE_500_IMPLEMENTATION_EXCEPTION))
            })
    @POST
    @Produces({MediaType.APPLICATION_JSON, "application/problem+json", "application/ld+json"})
    @Consumes({MediaType.APPLICATION_JSON, "application/ld+json"})
    @RequestBody(
            required = true,
            content =
            @Content(
                    schema = @Schema(implementation = EPCISQuery.class),
                    example =
                            "{\n"
                                    + "  \"query\": {\n"
                                    + "    \"EQ_bizStep\": [\n"
                                    + "      \"shipping\",\n"
                                    + "      \"receiving\"\n"
                                    + "    ],\n"
                                    + "    \"eventType\": [\n"
                                    + "      \"ObjectEvent\"\n"
                                    + "    ]\n"
                                    + "  },\n"
                                    + "  \"name\": \"myQuery\"\n"
                                    + "}"))
    public Uni<Response> queriesQueryNamePost(
            @RestHeader(value = "GS1-EPCIS-Version")
            @DefaultValue(DEFAULT_EPCIS_VERSION_PARAMETER_VALUE)
            String epcisVersion,
            @RestHeader(value = "GS1-CBV-Version")
            @DefaultValue(DEFAULT_EPCIS_VERSION_PARAMETER_VALUE)
            String cbvVersion,
            @Valid String epcisQuery,
            @Context HttpServerRequest request);

    @Operation(description = "", summary = "Returns the query definition.")
    @Parameters(
            value = {
                    @Parameter(
                            name = "GS1-CBV-Min",
                            description = ParameterDescriptions.GS1_CBV_MIN,
                            in = ParameterIn.HEADER,
                            content = @Content(example = DEFAULT_CBV_MIN_PARAMETER_VALUE)),
                    @Parameter(
                            name = "GS1-CBV-Max",
                            description = ParameterDescriptions.GS1_CBV_MAX,
                            in = ParameterIn.HEADER,
                            content = @Content(example = DEFAULT_CBV_MAX_PARAMETER_VALUE)),
                    @Parameter(
                            name = "GS1-EPCIS-Min",
                            description = ParameterDescriptions.GS1_EPCIS_MIN,
                            in = ParameterIn.HEADER,
                            content = @Content(example = DEFAULT_EPCIS_MIN_PARAMETER_VALUE)),
                    @Parameter(
                            name = "GS1-EPCIS-Max",
                            description = ParameterDescriptions.GS1_EPCIS_MAX,
                            in = ParameterIn.HEADER,
                            content = @Content(example = DEFAULT_EPCIS_MAX_PARAMETER_VALUE)),
                    @Parameter(
                            name = "GS1-Extensions",
                            description = ParameterDescriptions.GS1_EXTENSIONS,
                            in = ParameterIn.HEADER),
                    @Parameter(
                            description = "The name of an EPCIS event query",
                            required = true,
                            in = ParameterIn.PATH)
            }
    )
    @APIResponses(
            value = {
                    @APIResponse(
                            responseCode = "200",
                            description =
                                    "Queries are like views that are created using the <a href=\"https://github.com/gs1/EPCIS/tree/master/REST%20Bindings/query-schema.json\">EPCIS Query Language</a>. Each query object\n"
                                            + "consists of a query name and the query definition. "
                                            + "An EPCIS 2.0 query body using the REST interface SHALL be serialised as a JSON object. The value of the query key within that JSON object SHALL validate against the schema defined at:  https://ref.gs1.org/standards/epcis/2.0.0/query-schema.json.",
                            headers = {
                                    @Header(
                                            name = "GS1-EPCIS-version",
                                            description = ParameterDescriptions.GS1_EPCIS_VERSION,
                                            schema = @Schema(implementation = String.class)),
                                    @Header(
                                            name = "GS1-CBV-version",
                                            description = ParameterDescriptions.GS1_CBV_VERSION,
                                            schema = @Schema(implementation = String.class)),
                                    @Header(
                                            name = "GS1-Extensions",
                                            description = ParameterDescriptions.GS1_EXTENSIONS,
                                            schema = @Schema(implementation = String.class))
                            },
                            content =
                            @Content(
                                    schema = @Schema(anyOf = Object.class),
                                    example =
                                            "{\n"
                                                    + "  \"name\": \"myQuery\",\n"
                                                    + "  \"query\": {\n"
                                                    + "    \"EQ_bizStep\": [\n"
                                                    + "      \"shipping\",\n"
                                                    + "      \"receiving\"\n"
                                                    + "    ],\n"
                                                    + "    \"eventType\": [\n"
                                                    + "      \"ObjectEvent\"\n"
                                                    + "    ]\n"
                                                    + "  }\n"
                                                    + "}")),
                    @APIResponse(
                            responseCode = "401",
                            description = "Authorization information is missing or invalid.",
                            content =
                            @Content(
                                    schema = @Schema(implementation = ProblemResponseBody.class),
                                    example = ResponseBodyExamples.RESPONSE_401_UNAUTHORIZED_REQUEST)),
                    @APIResponse(
                            responseCode = "403",
                            description = "Client is unauthorized to access this resource",
                            content =
                            @Content(
                                    schema = @Schema(implementation = ProblemResponseBody.class),
                                    example = ResponseBodyExamples.RESPONSE_403_CLIENT_UNAUTHORIZED)),
                    @APIResponse(
                            responseCode = "404",
                            description = "Query not found.",
                            content =
                            @Content(
                                    schema = @Schema(implementation = ProblemResponseBody.class),
                                    example = ResponseBodyExamples.RESPONSE_404_RESOURCE_NOT_FOUND)),
                    @APIResponse(
                            responseCode = "406",
                            description = "The server cannot return the response as requested.",
                            content =
                            @Content(
                                    schema = @Schema(implementation = ProblemResponseBody.class),
                                    example = ResponseBodyExamples.RESPONSE_406_NOT_ACCEPTABLE)),
                    @APIResponse(
                            responseCode = "413",
                            description = "Query result is too large.",
                            content =
                            @Content(
                                    schema = @Schema(implementation = ProblemResponseBody.class),
                                    example = ResponseBodyExamples.RESPONSE_413_QUERY_SCOPE_OR_SIZE)),
                    @APIResponse(
                            responseCode = "500",
                            description =
                                    "This is a server-side problem caused when the query was resolved and executed.",
                            content =
                            @Content(
                                    schema = @Schema(implementation = ProblemResponseBody.class),
                                    example = ResponseBodyExamples.RESPONSE_500_IMPLEMENTATION_EXCEPTION))
            })
    @GET
    @Path("{queryName}")
    @Produces({MediaType.APPLICATION_JSON, "application/problem+json", "application/ld+json"})
    @Consumes({MediaType.APPLICATION_JSON, "application/ld+json"})
    public Uni<Response> queriesQueryNameGet(@RestPath String queryName);

    @Tags(
            value = {
                    @Tag(
                            name = "Events",
                            description = "Endpoints that allow you to retrieve EPCIS events as Web resources."),
                    @Tag(
                            name = "Queries",
                            description =
                                    "Endpoints to create large named or anonymous queries using the EPCIS Query Language. Named queries have a custom\n"
                                            + "      name and are stored until deleted by the user. Anonymous queries are not persisted and only available to the caller.\n"
                                            + "      EPCIS events queries also support query subscription.")
            })
    @Operation(
            description =
                    "The `GET` endpoint  is to retrieve results of a named query. Furthermore, this endpoint can also be used to subscribe to queries using Websocket. To do this, the client\n"
                            + "must specify the query schedule or set the `stream` parameter to `true` as a URL query string parameter. Please note that scheduling parameters and the `stream` parameter are mutually exclusive.\n"
                            + "## Scheduled query: Receive query results at 1.05am\n"
                            + "Handshake from client for scheduled query:\n"
                            + "```\n"
                            + "GET https://example.com/queries/MyQuery/events?minute=5&hour=1\n"
                            + "Host: example.com\n"
                            + "Upgrade: websocket\n"
                            + "Connection: Upgrade\n"
                            + "```\n"
                            + "Handshake from the server:\n"
                            + "```\n"
                            + "HTTP/1.1 101 Switching Protocols\n"
                            + "Upgrade: websocket\n"
                            + "Connection: Upgrade\n"
                            + "```\n"
                            + "## Streaming query subscription: Whenever a captured EPCIS event matches the query criteria\n"
                            + "Handshake from client for streaming:\n"
                            + "```\n"
                            + "GET https://example.com/queries/MyQuery/events?stream=true\n"
                            + "Host: example.com\n"
                            + "Upgrade: websocket\n"
                            + "Connection: Upgrade\n"
                            + "```\n"
                            + "Handshake from the server:\n"
                            + "```\n"
                            + "HTTP/1.1 101 Switching Protocols\n"
                            + "Upgrade: websocket\n"
                            + "Connection: Upgrade\n"
                            + "```",
            summary =
                    "Returns EPCIS events with the option to use pagination if needed. This endpoint supports query subscriptions using WebSockets.")
    @Parameters(
            value = {
                    @Parameter(
                            name = "GS1-EPC-Format",
                            description = ParameterDescriptions.GS1_EPC_FORMAT,
                            in = ParameterIn.HEADER,
                            content = @Content(schema = @Schema(implementation = EPCFormat.class))),
                    @Parameter(
                            name = "GS1-CBV-XML-Format",
                            description = ParameterDescriptions.GS1_CBV_XML_FORMAT,
                            in = ParameterIn.HEADER,
                            content = @Content(schema = @Schema(implementation = CBVFormat.class))),
                    @Parameter(
                            name = "GS1-CBV-Min",
                            description = ParameterDescriptions.GS1_CBV_MIN,
                            in = ParameterIn.HEADER,
                            content = @Content(example = DEFAULT_CBV_MIN_PARAMETER_VALUE)),
                    @Parameter(
                            name = "GS1-CBV-Max",
                            description = ParameterDescriptions.GS1_CBV_MAX,
                            in = ParameterIn.HEADER,
                            content = @Content(example = DEFAULT_CBV_MAX_PARAMETER_VALUE)),
                    @Parameter(
                            name = "GS1-EPCIS-Min",
                            description = ParameterDescriptions.GS1_EPCIS_MIN,
                            in = ParameterIn.HEADER,
                            content = @Content(example = DEFAULT_EPCIS_MIN_PARAMETER_VALUE)),
                    @Parameter(
                            name = "GS1-EPCIS-Max",
                            description = ParameterDescriptions.GS1_EPCIS_MAX,
                            in = ParameterIn.HEADER,
                            content = @Content(example = DEFAULT_EPCIS_MAX_PARAMETER_VALUE)),
                    @Parameter(
                            name = "GS1-Extensions",
                            description = ParameterDescriptions.GS1_EXTENSIONS,
                            in = ParameterIn.HEADER),
                    @Parameter(name = "Upgrade", description = "", example = "websocket", in = ParameterIn.HEADER),
                    @Parameter(
                            name = "Connection",
                            description = "",
                            example = "upgrade",
                            in = ParameterIn.HEADER),
                    @Parameter(
                            name = "perPage",
                            description = ParameterDescriptions.PER_PAGE,
                            in = ParameterIn.QUERY),
                    @Parameter(
                            name = "nextPageToken",
                            description = ParameterDescriptions.NEXT_PAGE_TOKEN,
                            in = ParameterIn.QUERY,
                            content = @Content(example = "3A15506738749783AU6D7DENAKwM2gQRRwGritaeq")),
                    @Parameter(
                            name = "initialRecordTime",
                            description = ParameterDescriptions.INITIAL_RECORD_TIME,
                            in = ParameterIn.QUERY,
                            content = @Content(example = "2020-04-04T20:33:31.116-06:00")),
                    @Parameter(
                            name = "reportIfEmpty",
                            description = ParameterDescriptions.REPORT_IF_EMPTY,
                            example = "false",
                            in = ParameterIn.QUERY),
                    @Parameter(
                            name = "second",
                            description = ParameterDescriptions.SECOND,
                            in = ParameterIn.QUERY,
                            content = @Content(example = "0")),
                    @Parameter(
                            name = "minute",
                            description = ParameterDescriptions.MINUTE,
                            in = ParameterIn.QUERY,
                            content = @Content(example = "5")),
                    @Parameter(
                            name = "hour",
                            description = ParameterDescriptions.HOUR,
                            in = ParameterIn.QUERY,
                            content = @Content(example = "1")),
                    @Parameter(
                            name = "dayOfMonth",
                            description = ParameterDescriptions.DAY_OF_MONTH,
                            in = ParameterIn.QUERY,
                            content = @Content(example = "1")),
                    @Parameter(
                            name = "month",
                            description = ParameterDescriptions.MONTH,
                            in = ParameterIn.QUERY,
                            content = @Content(example = "1")),
                    @Parameter(
                            name = "dayOfWeek",
                            description = ParameterDescriptions.DAY_OF_WEEK,
                            in = ParameterIn.QUERY,
                            content = @Content(example = "1")),
                    @Parameter(name = "queryName", description = "The name of an EPCIS event query.", required = true, in = ParameterIn.PATH)
            }
    )
    @APIResponses(
            value = {
                    @APIResponse(
                            responseCode = "101",
                            description =
                                    "Create a WebSocket connection to subscribe to queries. Upon subscription, the server SHALL send all new events to subscribing clients. If multiple clients have the same query, each client will receive events which they are authorized to see.",
                            headers = {
                                    @Header(
                                            name = "Upgrade",
                                            description = "",
                                            schema = @Schema(implementation = String.class)),
                                    @Header(
                                            name = "Connection",
                                            description = "",
                                            schema = @Schema(implementation = String.class)),
                                    @Header(
                                            name = "GS1-Query-Min-Record-Time",
                                            description = "",
                                            schema = @Schema(implementation = String.class))
                            }),
                    @APIResponse(
                            responseCode = "200",
                            description = "Returns a list of EPCIS events that match the query named in the path.",
                            headers = {
                                    @Header(
                                            name = "GS1-EPCIS-version",
                                            description = ParameterDescriptions.GS1_EPCIS_VERSION,
                                            schema = @Schema(implementation = String.class)),
                                    @Header(
                                            name = "GS1-CBV-version",
                                            description = ParameterDescriptions.GS1_CBV_VERSION,
                                            schema = @Schema(implementation = String.class)),
                                    @Header(
                                            name = "GS1-Extensions",
                                            description = ParameterDescriptions.GS1_EXTENSIONS,
                                            schema = @Schema(implementation = String.class)),
                                    @Header(
                                            name = "Link",
                                            description = ParameterDescriptions.LINK,
                                            schema = @Schema(implementation = String.class)),
                                    @Header(
                                            name = "GS1-Next-Page-Token-Expires",
                                            description = ParameterDescriptions.GS1_NEXT_PAGE_TOKEN_EXPIRES,
                                            schema = @Schema(implementation = String.class))
                            },
                            content =
                            @Content(
                                    schema = @Schema(anyOf = Object.class),
                                    example = ResponseBodyExamples.RESPONSE_200_EPCIS_QUERY_DOCUMENT)),
                    @APIResponse(
                            responseCode = "400",
                            description = "The query is invalid",
                            content =
                            @Content(
                                    schema = @Schema(implementation = ProblemResponseBody.class),
                                    example = ResponseBodyExamples.RESPONSE_400_SUBSCRIPTION_ISSUES)),
                    @APIResponse(
                            responseCode = "401",
                            description = "Authorization information is missing or invalid.",
                            content =
                            @Content(
                                    schema = @Schema(implementation = ProblemResponseBody.class),
                                    example = ResponseBodyExamples.RESPONSE_401_UNAUTHORIZED_REQUEST)),
                    @APIResponse(
                            responseCode = "403",
                            description = "Client is unauthorized to access this resource.",
                            content =
                            @Content(
                                    schema = @Schema(implementation = ProblemResponseBody.class),
                                    example = ResponseBodyExamples.RESPONSE_403_CLIENT_UNAUTHORIZED)),
                    @APIResponse(
                            responseCode = "404",
                            description =
                                    "At least one resource was not found. For example, the EPCIS event does not exist or the query does not exist.",
                            content =
                            @Content(
                                    schema = @Schema(implementation = ProblemResponseBody.class),
                                    example = ResponseBodyExamples.RESPONSE_404_RESOURCE_NOT_FOUND)),
                    @APIResponse(
                            responseCode = "406",
                            description = "The server cannot return the response as requested.",
                            content =
                            @Content(
                                    schema = @Schema(implementation = ProblemResponseBody.class),
                                    example = ResponseBodyExamples.RESPONSE_406_NOT_ACCEPTABLE)),
                    @APIResponse(
                            responseCode = "413",
                            description = "Query result is too large.",
                            content =
                            @Content(
                                    schema = @Schema(implementation = ProblemResponseBody.class),
                                    example = ResponseBodyExamples.RESPONSE_413_QUERY_SCOPE_OR_SIZE)),
                    @APIResponse(
                            responseCode = "414",
                            description =
                                    "URL is too long. This is usually a problem with large EPCIS queries in the URL.",
                            content =
                            @Content(
                                    schema = @Schema(implementation = ProblemResponseBody.class),
                                    example = ResponseBodyExamples.RESPONSE_414_URL_TOO_LONG)),
                    @APIResponse(
                            responseCode = "500",
                            description =
                                    "This is a server-side problem caused when the query was resolved and executed.",
                            content =
                            @Content(
                                    schema = @Schema(implementation = ProblemResponseBody.class),
                                    example = ResponseBodyExamples.RESPONSE_500_IMPLEMENTATION_EXCEPTION))
            })
    @GET
    @Path("{queryName}/events")
    @Produces({
            MediaType.APPLICATION_JSON,
            MediaType.APPLICATION_XML,
            MediaType.TEXT_XML,
            "application/problem+json",
            "application/ld+json"
    })
    @Consumes({MediaType.APPLICATION_JSON, "application/ld+json"})
    public Uni<Response> queriesQueryNameEventsGet(
            @Context SecurityIdentity securityIdentity,
            @Valid
            @RestQuery
            @DefaultValue(DEFAULT_PER_PAGE_PARAMETER_VALUE)
            Integer perPage,
            @Valid
            @RestQuery
            String nextPageToken,
            @RestPath
            String queryName,
            @Context UriInfo uriInfo);

    @Tags(
            value = {
                    @Tag(
                            name = "Queries",
                            description =
                                    "Endpoints to create large named or anonymous queries using the EPCIS Query Language. Named queries have a custom\n"
                                            + "      name and are stored until deleted by the user. Anonymous queries are not persisted and only available to the caller.\n"
                                            + "      EPCIS events queries also support query subscription."),
                    @Tag(name = "Discovery", description = "Endpoints to obtain information about the endpoint, such as EPCIS and CBV versions or custom vocabularies.")
            })
    @Operation(
            description =
                    "EPCIS 2.0 supports a number of custom headers to describe custom vocabularies and support multiple versions "
                            + "of EPCIS and CBV. The `OPTIONS` method allows the client to discover which vocabularies and EPCIS and CBV "
                            + "versions are used.",
            summary = "Query the metadata of the EPCIS queries endpoint.")
    @Parameters(
            value = {
                    @Parameter(
                            name = "GS1-Extensions",
                            description = ParameterDescriptions.GS1_EXTENSIONS,
                            in = ParameterIn.HEADER)
            }
    )
    @APIResponses(
            value = {
                    @APIResponse(
                            responseCode = "204",
                            description =
                                    "The `OPTIONS` method is used to discover capabilities for EPCIS 2.0 endpoints. It describes which EPCIS and CBV versions are supported and used for the top-level resource "
                                            + "as well as EPCIS and CBV extensions. The list of headers is not exhaustive. It only describes the functionality specific to EPCIS 2.0.",
                            content = @Content(schema = @Schema(implementation = Object.class)),
                            headers = {
                                    @Header(
                                            name = "Allow",
                                            description = ParameterDescriptions.ALLOW,
                                            schema = @Schema(implementation = String.class)),
                                    @Header(
                                            name = "GS1-EPCIS-version",
                                            description = ParameterDescriptions.GS1_EPCIS_VERSION,
                                            schema = @Schema(implementation = String.class)),
                                    @Header(
                                            name = "GS1-EPCIS-Min",
                                            description = ParameterDescriptions.GS1_EPCIS_MIN,
                                            schema = @Schema(implementation = String.class)),
                                    @Header(
                                            name = "GS1-EPCIS-Max",
                                            description = ParameterDescriptions.GS1_EPCIS_MAX,
                                            schema = @Schema(implementation = String.class)),
                                    @Header(
                                            name = "GS1-CBV-Version",
                                            description = ParameterDescriptions.GS1_CBV_VERSION,
                                            schema = @Schema(implementation = String.class)),
                                    @Header(
                                            name = "GS1-CBV-Min",
                                            description = ParameterDescriptions.GS1_CBV_MIN,
                                            schema = @Schema(implementation = String.class)),
                                    @Header(
                                            name = "GS1-CBV-Max",
                                            description = ParameterDescriptions.GS1_CBV_MAX,
                                            schema = @Schema(implementation = String.class)),
                                    @Header(
                                            name = "GS1-EPC-Format",
                                            description = ParameterDescriptions.GS1_EPC_FORMAT,
                                            schema = @Schema(implementation = String.class)),
                                    @Header(
                                            name = "GS1-CBV-XML-Format",
                                            description = ParameterDescriptions.GS1_CBV_XML_FORMAT,
                                            schema = @Schema(implementation = String.class)),
                                    @Header(
                                            name = "GS1-Extensions",
                                            description = ParameterDescriptions.GS1_EXTENSIONS,
                                            schema = @Schema(implementation = Object.class)),
                                    @Header(
                                            name = "GS1-Vendor-Version",
                                            description = ParameterDescriptions.GS1_VENDOR_VERSION,
                                            schema = @Schema(implementation = Object.class))
                            }),
                    @APIResponse(
                            responseCode = "401",
                            description = "Authorization information is missing or invalid.",
                            content =
                            @Content(
                                    schema = @Schema(implementation = ProblemResponseBody.class),
                                    example = ResponseBodyExamples.RESPONSE_401_UNAUTHORIZED_REQUEST)),
                    @APIResponse(
                            responseCode = "403",
                            description = "Client is unauthorized to access this resource",
                            content =
                            @Content(
                                    schema = @Schema(implementation = ProblemResponseBody.class),
                                    example = ResponseBodyExamples.RESPONSE_403_CLIENT_UNAUTHORIZED)),
                    @APIResponse(
                            responseCode = "406",
                            description = "The server cannot return the response as requested.",
                            content =
                            @Content(
                                    schema = @Schema(implementation = Object.class),
                                    example = ResponseBodyExamples.RESPONSE_406_NOT_ACCEPTABLE)),
                    @APIResponse(
                            responseCode = "500",
                            description = "An error occurred on the backend.",
                            content =
                            @Content(
                                    schema = @Schema(implementation = ProblemResponseBody.class),
                                    example = ResponseBodyExamples.RESPONSE_500_IMPLEMENTATION_EXCEPTION))
            })
    @OPTIONS
    @Produces({MediaType.APPLICATION_JSON, "application/ld+json"})
    @Consumes({MediaType.APPLICATION_JSON, "application/ld+json"})
    public Response queriesOptions();

    @Operation(
            description = "",
            summary =
                    "Removes a named query and forcibly unsubscribes all active subscriptions, weather by WebSockets or Webhooks")
    @Parameters(
            value = {
                    @Parameter(description = "", required = true, in = ParameterIn.PATH)
            }
    )
    @APIResponses(
            value = {
                    @APIResponse(responseCode = "204", description = "Query deleted and clients disconnected."),
                    @APIResponse(
                            responseCode = "401",
                            description = "Authorization information is missing or invalid.",
                            content =
                            @Content(
                                    schema = @Schema(implementation = ProblemResponseBody.class),
                                    example = ResponseBodyExamples.RESPONSE_401_UNAUTHORIZED_REQUEST)),
                    @APIResponse(
                            responseCode = "403",
                            description = "Client is unauthorized to access this resource",
                            content =
                            @Content(
                                    schema = @Schema(implementation = ProblemResponseBody.class),
                                    example = ResponseBodyExamples.RESPONSE_403_CLIENT_UNAUTHORIZED)),
                    @APIResponse(
                            responseCode = "404",
                            description = "Named query not found.",
                            content =
                            @Content(
                                    schema = @Schema(implementation = ProblemResponseBody.class),
                                    example = ResponseBodyExamples.RESPONSE_404_RESOURCE_NOT_FOUND)),
                    @APIResponse(
                            responseCode = "406",
                            description = "The server cannot return the response as requested.",
                            content =
                            @Content(
                                    schema = @Schema(implementation = ProblemResponseBody.class),
                                    example = ResponseBodyExamples.RESPONSE_406_NOT_ACCEPTABLE)),
                    @APIResponse(
                            responseCode = "500",
                            description = "This is a server-side problem.",
                            content =
                            @Content(
                                    schema = @Schema(implementation = ProblemResponseBody.class),
                                    example = ResponseBodyExamples.RESPONSE_500_IMPLEMENTATION_EXCEPTION))
            })
    @DELETE
    @Path("{queryName}")
    @Produces({"application/problem+json", "application/ld+json"})
    @Consumes({MediaType.APPLICATION_JSON, "application/ld+json"})
    public Uni<Response> queriesQueryNameDelete(@RestPath String queryName);

    @Tags(
            value = {
                    @Tag(
                            name = "Queries",
                            description =
                                    "Endpoints to create large named or anonymous queries using the EPCIS Query Language. Named queries have a custom\n"
                                            + "      name and are stored until deleted by the user. Anonymous queries are not persisted and only available to the caller.\n"
                                            + "      EPCIS events queries also support query subscription."),
                    @Tag(name = "Discovery", description = "Endpoints to obtain information about the endpoint, such as EPCIS and CBV versions or custom vocabularies.")
            })
    @Operation(
            description =
                    "The `OPTIONS` method is used to discover capabilities for named queries. It describes which EPCIS and CBV versions are used in the query result supported as well as EPCIS and CBV extensions.",
            summary = "Query the metadata of the EPCIS events query result endpoint.")
    @Parameters(
            value = {
                    @Parameter(description = "", required = true, in = ParameterIn.PATH),
                    @Parameter(
                            name = "GS1-Extensions",
                            description = ParameterDescriptions.GS1_EXTENSIONS,
                            in = ParameterIn.HEADER)
            }
    )
    @APIResponses(
            value = {
                    @APIResponse(
                            responseCode = "204",
                            description =
                                    "Server can comply with the GS1-EPCIS-related requirements from the client",
                            content = @Content(schema = @Schema(implementation = Object.class)),
                            headers = {
                                    @Header(
                                            name = "Allow",
                                            description = ParameterDescriptions.ALLOW,
                                            schema = @Schema(implementation = String.class)),
                                    @Header(
                                            name = "GS1-EPCIS-version",
                                            description = ParameterDescriptions.GS1_EPCIS_VERSION,
                                            schema = @Schema(implementation = String.class)),
                                    @Header(
                                            name = "GS1-EPCIS-Min",
                                            description = ParameterDescriptions.GS1_EPCIS_MIN,
                                            schema = @Schema(implementation = String.class)),
                                    @Header(
                                            name = "GS1-EPCIS-Max",
                                            description = ParameterDescriptions.GS1_EPCIS_MAX,
                                            schema = @Schema(implementation = String.class)),
                                    @Header(
                                            name = "GS1-CBV-Version",
                                            description = ParameterDescriptions.GS1_CBV_VERSION,
                                            schema = @Schema(implementation = String.class)),
                                    @Header(
                                            name = "GS1-CBV-Min",
                                            description = ParameterDescriptions.GS1_CBV_MIN,
                                            schema = @Schema(implementation = String.class)),
                                    @Header(
                                            name = "GS1-CBV-Max",
                                            description = ParameterDescriptions.GS1_CBV_MAX,
                                            schema = @Schema(implementation = String.class)),
                                    @Header(
                                            name = "GS1-EPC-Format",
                                            description = ParameterDescriptions.GS1_EPC_FORMAT,
                                            schema = @Schema(implementation = String.class)),
                                    @Header(
                                            name = "GS1-CBV-XML-Format",
                                            description = ParameterDescriptions.GS1_CBV_XML_FORMAT,
                                            schema = @Schema(implementation = String.class)),
                                    @Header(
                                            name = "GS1-Extensions",
                                            description = ParameterDescriptions.GS1_EXTENSIONS,
                                            schema = @Schema(implementation = Object.class)),
                                    @Header(
                                            name = "GS1-Vendor-Version",
                                            description = ParameterDescriptions.GS1_VENDOR_VERSION,
                                            schema = @Schema(implementation = Object.class))
                            }),
                    @APIResponse(
                            responseCode = "401",
                            description = "Authorization information is missing or invalid.",
                            content =
                            @Content(
                                    schema = @Schema(implementation = ProblemResponseBody.class),
                                    example = ResponseBodyExamples.RESPONSE_401_UNAUTHORIZED_REQUEST)),
                    @APIResponse(
                            responseCode = "403",
                            description = "Client is unauthorized to access this resource",
                            content =
                            @Content(
                                    schema = @Schema(implementation = ProblemResponseBody.class),
                                    example = ResponseBodyExamples.RESPONSE_403_CLIENT_UNAUTHORIZED)),
                    @APIResponse(
                            responseCode = "404",
                            description = "The EPCIS event does not exist.",
                            content =
                            @Content(
                                    schema = @Schema(implementation = Object.class),
                                    example = ResponseBodyExamples.RESPONSE_404_RESOURCE_NOT_FOUND)),
                    @APIResponse(
                            responseCode = "406",
                            description = "The server cannot return the response as requested.",
                            content =
                            @Content(
                                    schema = @Schema(implementation = Object.class),
                                    example = ResponseBodyExamples.RESPONSE_406_NOT_ACCEPTABLE)),
                    @APIResponse(
                            responseCode = "414",
                            description = "URL is too long. This is usually a problem with large EPCIS queries in the URL.\n",
                            content =
                            @Content(
                                    schema = @Schema(implementation = Object.class),
                                    example = ResponseBodyExamples.RESPONSE_414_URL_TOO_LONG)),
                    @APIResponse(
                            responseCode = "500",
                            description = "An error occurred on the backend.",
                            content =
                            @Content(
                                    schema = @Schema(implementation = ProblemResponseBody.class),
                                    example = ResponseBodyExamples.RESPONSE_500_IMPLEMENTATION_EXCEPTION))
            })
    @OPTIONS
    @Path("{queryName}/events")
    @Produces({MediaType.APPLICATION_JSON, "application/ld+json"})
    @Consumes({MediaType.APPLICATION_JSON, "application/ld+json"})
    public Response queriesQueryNameEventsOptions();

    @Tags(
            value = {
                    @Tag(
                            name = "Queries",
                            description =
                                    "Endpoints to create large named or anonymous queries using the EPCIS Query Language. Named queries have a custom\n"
                                            + "      name and are stored until deleted by the user. Anonymous queries are not persisted and only available to the caller.\n"
                                            + "      EPCIS events queries also support query subscription."),
                    @Tag(name = "Discovery", description = "Endpoints to obtain information about the endpoint, such as EPCIS and CBV versions or custom vocabularies.")
            })
    @Operation(
            summary = "Query the metadata of the named queries endpoint.",
            description =
                    "EPCIS 2.0 supports a number of custom headers to describe custom vocabularies and support multiple versions of EPCIS and CBV. The `OPTIONS` method allows the client to discover which vocabularies and EPCIS and CBV versions are used.")
    @Parameters(
            value = {
                    @Parameter(description = "", required = false, in = ParameterIn.PATH),
                    @Parameter(
                            name = "GS1-Extensions",
                            description = ParameterDescriptions.GS1_EXTENSIONS,
                            in = ParameterIn.HEADER)
            }
    )
    @APIResponses(
            value = {
                    @APIResponse(
                            responseCode = "200",
                            description =
                                    "Server can comply with the GS1-EPCIS-related requirements from the client",
                            content = @Content(schema = @Schema(implementation = Object.class)),
                            headers = {
                                    @Header(
                                            name = "GS1-EPCIS-version",
                                            description = ParameterDescriptions.GS1_EPCIS_VERSION,
                                            schema = @Schema(implementation = String.class)),
                                    @Header(
                                            name = "GS1-EPCIS-min",
                                            description = ParameterDescriptions.GS1_EPCIS_MIN,
                                            schema = @Schema(implementation = String.class)),
                                    @Header(
                                            name = "GS1-EPCIS-max",
                                            description = ParameterDescriptions.GS1_EPCIS_MAX,
                                            schema = @Schema(implementation = String.class)),
                                    @Header(
                                            name = "GS1-CBV-version",
                                            description = ParameterDescriptions.GS1_CBV_VERSION,
                                            schema = @Schema(implementation = String.class)),
                                    @Header(
                                            name = "GS1-Extensions",
                                            description = ParameterDescriptions.GS1_EXTENSIONS,
                                            schema = @Schema(implementation = Object.class))
                            }),
                    @APIResponse(
                            responseCode = "401",
                            description = "Authorization information is missing or invalid.",
                            content =
                            @Content(
                                    schema = @Schema(implementation = ProblemResponseBody.class),
                                    example = ResponseBodyExamples.RESPONSE_401_UNAUTHORIZED_REQUEST)),
                    @APIResponse(
                            responseCode = "403",
                            description = "Client is unauthorized to access this resource",
                            content =
                            @Content(
                                    schema = @Schema(implementation = ProblemResponseBody.class),
                                    example = ResponseBodyExamples.RESPONSE_403_CLIENT_UNAUTHORIZED)),
                    @APIResponse(
                            responseCode = "404",
                            description = "The EPCIS event does not exist.",
                            content =
                            @Content(
                                    schema = @Schema(implementation = Object.class),
                                    example = ResponseBodyExamples.RESPONSE_404_RESOURCE_NOT_FOUND)),
                    @APIResponse(
                            responseCode = "406",
                            description = "The server cannot return the response as requested.",
                            content =
                            @Content(
                                    schema = @Schema(implementation = Object.class),
                                    example = ResponseBodyExamples.RESPONSE_406_NOT_ACCEPTABLE)),
                    @APIResponse(
                            responseCode = "500",
                            description = "An error occurred on the backend.",
                            content =
                            @Content(
                                    schema = @Schema(implementation = ProblemResponseBody.class),
                                    example = ResponseBodyExamples.RESPONSE_500_IMPLEMENTATION_EXCEPTION))
            })
    @OPTIONS
    @Path("{queryName}")
    @Produces({MediaType.APPLICATION_JSON, "application/ld+json"})
    @Consumes({MediaType.APPLICATION_JSON, "application/ld+json"})
    public Response queriesQueryNameOptions(
            @RestPath
            String queryName,
            @RestHeader(value = "GS1-Extensions")
            String gs1Extensions);

    @Tags(
            value = {
                    @Tag(
                            name = "Queries",
                            description =
                                    "Endpoints to create large named or anonymous queries using the EPCIS Query Language. Named queries have a custom\n"
                                            + "      name and are stored until deleted by the user. Anonymous queries are not persisted and only available to the caller.\n"
                                            + "      EPCIS events queries also support query subscription."),
                    @Tag(name = "Subscriptions", description = "EPCIS 2.0 supports query subscriptions using Webhooks or Websockets.")
            })
    @Operation(
            description = "The `GET` endpoint is to list all active subscriptions on that query.",
            summary = "Returns active subscriptions with the option to use pagination if needed.")
    @Parameters(
            value = {
                    @Parameter(description = "", required = true, in = ParameterIn.PATH),
                    @Parameter(
                            name = "perPage",
                            description = ParameterDescriptions.PER_PAGE,
                            in = ParameterIn.QUERY),
                    @Parameter(
                            name = "nextPageToken",
                            description = ParameterDescriptions.NEXT_PAGE_TOKEN,
                            in = ParameterIn.QUERY,
                            content = @Content(example = "3A15506738749783AU6D7DENAKwM2gQRRwGritaeq")),
                    @Parameter(
                            name = "GS1-EPCIS-min",
                            description = ParameterDescriptions.GS1_EPCIS_MIN,
                            in = ParameterIn.HEADER,
                            content = @Content(example = DEFAULT_EPCIS_MIN_PARAMETER_VALUE)),
                    @Parameter(
                            name = "GS1-EPCIS-max",
                            description = ParameterDescriptions.GS1_EPCIS_MAX,
                            in = ParameterIn.HEADER,
                            content = @Content(example = DEFAULT_EPCIS_MAX_PARAMETER_VALUE)),
                    @Parameter(
                            name = "GS1-Extensions",
                            description = ParameterDescriptions.GS1_EXTENSIONS,
                            in = ParameterIn.HEADER)
            }
    )
    @APIResponses(
            value = {
                    @APIResponse(
                            responseCode = "200",
                            description =
                                    "The `GET` endpoint is to list all open Webhook query subscriptions on that query.",
                            headers = {
                                    @Header(
                                            name = "GS1-EPCIS-version",
                                            description = ParameterDescriptions.GS1_EPCIS_VERSION,
                                            schema = @Schema(implementation = String.class)),
                                    @Header(
                                            name = "GS1-Extensions",
                                            description = ParameterDescriptions.GS1_EXTENSIONS,
                                            schema = @Schema(implementation = String.class)),
                                    @Header(
                                            name = "Link",
                                            description = ParameterDescriptions.LINK,
                                            schema = @Schema(implementation = String.class)),
                                    @Header(
                                            name = "GS1-Next-Page-Token-Expires",
                                            description = ParameterDescriptions.GS1_NEXT_PAGE_TOKEN_EXPIRES,
                                            schema = @Schema(implementation = String.class))
                            },
                            content =
                            @Content(
                                    example =
                                            "[\n"
                                                    + "  {\n"
                                                    + "    \"subscriptionID\": \"df5a33e3-5aa3-4403-ae01-99b83234e27b\",\n"
                                                    + "    \"createdAt\": \"2017-08-21T17:32:28Z\",\n"
                                                    + "    \"schedule\": {\n"
                                                    + "      \"hour\": \"1\",\n"
                                                    + "      \"minute\": \"5\"\n"
                                                    + "    }\n"
                                                    + "  },\n"
                                                    + "  {\n"
                                                    + "    \"subscriptionID\": \"df5a33e3-5aa3-5504-bf12-88c94345f38c\",\n"
                                                    + "    \"createdAt\": \"2017-07-21T17:32:28Z\",\n"
                                                    + "    \"stream\": true\n"
                                                    + "  }\n"
                                                    + "]")),
                    @APIResponse(
                            responseCode = "400",
                            description = "An issue with a subscription occurred.",
                            content =
                            @Content(
                                    schema = @Schema(implementation = ProblemResponseBody.class),
                                    example = ResponseBodyExamples.RESPONSE_400_SUBSCRIPTION_ISSUES)),
                    @APIResponse(
                            responseCode = "401",
                            description = "Authorization information is missing or invalid.",
                            content =
                            @Content(
                                    schema = @Schema(implementation = ProblemResponseBody.class),
                                    example = ResponseBodyExamples.RESPONSE_401_UNAUTHORIZED_REQUEST)),
                    @APIResponse(
                            responseCode = "403",
                            description = "Client is unauthorized to access this resource",
                            content =
                            @Content(
                                    schema = @Schema(implementation = ProblemResponseBody.class),
                                    example = ResponseBodyExamples.RESPONSE_403_CLIENT_UNAUTHORIZED)),
                    @APIResponse(
                            responseCode = "404",
                            description = "Subscriptions not found.",
                            content =
                            @Content(
                                    schema = @Schema(implementation = ProblemResponseBody.class),
                                    example = ResponseBodyExamples.RESPONSE_404_RESOURCE_NOT_FOUND)),
                    @APIResponse(
                            responseCode = "406",
                            description = "The server cannot return the response as requested.",
                            content =
                            @Content(
                                    schema = @Schema(implementation = ProblemResponseBody.class),
                                    example = ResponseBodyExamples.RESPONSE_406_NOT_ACCEPTABLE)),
                    @APIResponse(
                            responseCode = "500",
                            description = "This is a server-side problem.",
                            content =
                            @Content(
                                    schema = @Schema(implementation = ProblemResponseBody.class),
                                    example = ResponseBodyExamples.RESPONSE_500_IMPLEMENTATION_EXCEPTION))
            })
    @GET
    @Path("{queryName}/subscriptions")
    @Produces({MediaType.APPLICATION_JSON, "application/problem+json", "application/ld+json"})
    @Consumes({MediaType.APPLICATION_JSON, "application/ld+json"})
    public Uni<Response> queriesQueryNameSubscriptionGet(
            @RestPath
            String queryName,
            @Valid
            @RestQuery
            @DefaultValue(DEFAULT_PER_PAGE_PARAMETER_VALUE)
            Integer perPage,
            @Valid
            @RestQuery
            String nextPageToken);

    @Tags(
            value = {
                    @Tag(
                            name = "Queries",
                            description =
                                    "Endpoints to create large named or anonymous queries using the EPCIS Query Language. Named queries have a custom\n"
                                            + "      name and are stored until deleted by the user. Anonymous queries are not persisted and only available to the caller.\n"
                                            + "      EPCIS events queries also support query subscription."),
                    @Tag(name = "Subscriptions", description = "EPCIS 2.0 supports query subscriptions using Webhooks or Websockets.")
            })
    @Operation(
            description =
                    "EPCIS 2.0 implementations must support Webhook subscriptions. Creating a query subscription requires the client to provide a single endpoint to which the"
                            + "server will send events (as `EPCISQueryDocument`) and an optional string `signatureToken`."
                            + "This `signatureToken` must be generated by the client and is used by the server to authenticate itself and sign messages when sending events. The signature must be contained on the `GS1-Signature` HTTP header of the server request. \n"
                            + "\n"
                            + "The choice of signature type is implementation specific but examples would be using HMAC with SHA-256 directly or a wrapper supporting various symmetric or asymetric \n"
                            + "cryptographic algorithms such as Json Web Signature (JWS). When the client subscribes to a query, it must either set `stream` to `true`, to be notified whenever a new EPCIS\n"
                            + "event matches the query, or the client must define a query schedule. If these are missing the query subscription is invalid because the server won't know when to notify a client.\n"
                            + "## Scheduled query: Receive query results at 1.05am\n"
                            + "A scheduled query subscription is a time-based query execution. EPCIS 2.0 scheduled queries are scheduled\n"
                            + "in the same manner as cron jobs.\n"
                            + "For example, this query subscription is scheduled to trigger every morning at 1.05am. By setting\n"
                            + "`reportIfEmpty` to `true`, the client's delivery URL (`dest`) will be called even if there are no new events that match\n"
                            + "the query.\n"
                            + "```\n"
                            + "POST /queries/MyQuery/subscriptions\n"
                            + "{\n"
                            + "  \"dest\": \"https://client.example.com/queryCallback\",\n"
                            + "  \"signatureToken\": \"13df38d8275b13f05704629e5f1cf3d45d6132d5\",\n"
                            + "  \"reportIfEmpty\": true,\n"
                            + "  \"schedule\": {\n"
                            + "    \"hour\":\"1\",\n"
                            + "    \"minute\": \"5\"\n"
                            + "  }\n"
                            + "}\n"
                            + "```\n"
                            + "## Streaming query subscription: Whenever a captured EPCIS event matches the query criteria\n"
                            + "If no query schedule is specified, the client must explicitly set `stream` to `true`. This restriction is to prevent clients from accidentally subscribing to EPCIS event streams.\n"
                            + "```\n"
                            + "POST /queries/MyQuery/subscriptions\n"
                            + "{\n"
                            + "  \"dest\": \"https://client.example.com/queryCallback\",\n"
                            + "  \"signatureToken\": \"13df38d8275b13f05704629e5f1cf3d45d6132d5\",\n"
                            + "  \"stream\": true\n"
                            + "}\n"
                            + "```",
            summary = "Creates a query subscription")
    @Parameters(
            value = {
                    @Parameter(description = "", required = true, in = ParameterIn.PATH),
                    @Parameter(
                            name = "GS1-EPCIS-Version",
                            description = ParameterDescriptions.GS1_EPCIS_VERSION,
                            in = ParameterIn.HEADER),
                    @Parameter(
                            name = "GS1-EPC-Format",
                            description = ParameterDescriptions.GS1_EPC_FORMAT,
                            in = ParameterIn.HEADER,
                            content = @Content(schema = @Schema(implementation = EPCFormat.class))),
                    @Parameter(
                            name = "GS1-CBV-XML-Format",
                            description = ParameterDescriptions.GS1_CBV_XML_FORMAT,
                            in = ParameterIn.HEADER,
                            content = @Content(schema = @Schema(implementation = CBVFormat.class))),
                    @Parameter(
                            name = "GS1-Extensions",
                            description = ParameterDescriptions.GS1_EXTENSIONS,
                            in = ParameterIn.HEADER)
            }
    )
    @APIResponses(
            value = {
                    @APIResponse(
                            responseCode = "201",
                            description =
                                    "Query subscription successful. The subscription is valid until the client unsubscribes.",
                            headers = {
                                    @Header(
                                            name = "Location",
                                            description =
                                                    "The server creates a new subscription for each client and query subscription. The client needs that URL"
                                                            + " to unsubscribe by deleting this resource. The `Location` URL must point to the `subscriptionID` returned"
                                                            + " in the response body. Note that for security reasons the response should not return the secret.")
                            },
                            content =
                            @Content(
                                    schema = @Schema(implementation = String.class),
                                    example =
                                            "{\n"
                                                    + "  \"dest\": \"https://client.example.com/queryCallback\",\n"
                                                    + "  \"subscriptionID\": \"df5a33e3-5aa3-4403-ae01-99b83234e27b\",\n"
                                                    + "  \"stream\": true,\n"
                                                    + "  \"createdAt\": \"2017-08-21T17:32:28Z\",\n"
                                                    + "  \"lastNotifiedAt\": \"2022-01-08T06:51:21.889Z\"\n"
                                                    + "}")),
                    @APIResponse(
                            responseCode = "401",
                            description = "Authorization information is missing or invalid.",
                            content =
                            @Content(
                                    schema = @Schema(implementation = ProblemResponseBody.class),
                                    example = ResponseBodyExamples.RESPONSE_401_UNAUTHORIZED_REQUEST)),
                    @APIResponse(
                            responseCode = "403",
                            description = "Client is unauthorized to access this resource",
                            content =
                            @Content(
                                    schema = @Schema(implementation = ProblemResponseBody.class),
                                    example = ResponseBodyExamples.RESPONSE_403_CLIENT_UNAUTHORIZED)),
                    @APIResponse(
                            responseCode = "404",
                            description = "Resource not found.",
                            content =
                            @Content(
                                    schema = @Schema(implementation = ProblemResponseBody.class),
                                    example = ResponseBodyExamples.RESPONSE_404_RESOURCE_NOT_FOUND)),
                    @APIResponse(
                            responseCode = "406",
                            description = "The server cannot return the response as requested.",
                            content =
                            @Content(
                                    schema = @Schema(implementation = ProblemResponseBody.class),
                                    example = ResponseBodyExamples.RESPONSE_406_NOT_ACCEPTABLE)),
                    @APIResponse(
                            responseCode = "415",
                            description = "The client sent data in a format that is not supported by the server.",
                            content =
                            @Content(
                                    schema = @Schema(implementation = ProblemResponseBody.class),
                                    example = ResponseBodyExamples.RESPONSE_415_UNSUPPORTED_MEDIA_TYPE)),
                    @APIResponse(
                            responseCode = "500",
                            description = "This is a server-side problem.",
                            content =
                            @Content(
                                    schema = @Schema(implementation = ProblemResponseBody.class),
                                    example = ResponseBodyExamples.RESPONSE_500_IMPLEMENTATION_EXCEPTION))
            })
    @POST
    @Path("{queryName}/subscriptions")
    @Produces({MediaType.APPLICATION_JSON, "application/problem+json", "application/ld+json"})
    @Consumes({MediaType.APPLICATION_JSON, "application/ld+json"})
    @RequestBody(
            content =
            @Content(
                    example =
                            "{\n"
                                    + "  \"dest\": \"https://client.example.com/queryCallback\",\n"
                                    + "  \"signatureToken\": \"13df38d8275b13f05704629e5f1cf3d45d6132d5\",\n"
                                    + "  \"schedule\": {\n"
                                    + "    \"hour\": \"1\",\n"
                                    + "    \"minute\": \"5\"\n"
                                    + "  }\n"
                                    + "}"))
    public Uni<Response> queriesQueryNameSubscriptionPost(
            String querySubscription,
            @RestPath
            String queryName,
            @RestHeader(value = "GS1-EPCIS-Version")
            @DefaultValue(DEFAULT_EPCIS_VERSION_PARAMETER_VALUE)
            String epcisVersion,
            @RestHeader(value = "GS1-EPC-Format")
            String epcFormat,
            @RestHeader(value = "GS1-CBV-XML-Format")
            String cbvFormat);

    @Tags(
            value = {
                    @Tag(
                            name = "Queries",
                            description =
                                    "Endpoints to create large named or anonymous queries using the EPCIS Query Language. Named queries have a custom\n"
                                            + "      name and are stored until deleted by the user. Anonymous queries are not persisted and only available to the caller.\n"
                                            + "      EPCIS events queries also support query subscription."),
                    @Tag(name = "Subscriptions", description = "EPCIS 2.0 supports query subscriptions using Webhooks or Websockets.")
            })
    @Operation(
            description = "",
            summary =
                    "Returns the details of a subscription. This method is useful to verify if a subscription is still active")
    @Parameters(
            value = {
                    @Parameter(description = "", required = true, in = ParameterIn.PATH),
                    @Parameter(description = "", required = true, in = ParameterIn.PATH),
                    @Parameter(
                            name = "GS1-EPCIS-min",
                            description = ParameterDescriptions.GS1_EPCIS_MIN,
                            in = ParameterIn.HEADER,
                            content = @Content(example = DEFAULT_EPCIS_MIN_PARAMETER_VALUE)),
                    @Parameter(
                            name = "GS1-EPCIS-max",
                            description = ParameterDescriptions.GS1_EPCIS_MAX,
                            in = ParameterIn.HEADER,
                            content = @Content(example = DEFAULT_EPCIS_MAX_PARAMETER_VALUE)),
                    @Parameter(
                            name = "GS1-Extensions",
                            description = ParameterDescriptions.GS1_EXTENSIONS,
                            in = ParameterIn.HEADER)
            }
    )
    @APIResponses(
            value = {
                    @APIResponse(
                            responseCode = "200",
                            description =
                                    "The `GET` method on a query subscription allows the client to see individual subscriptions.",
                            headers = {
                                    @Header(
                                            name = "GS1-EPCIS-version",
                                            description = ParameterDescriptions.GS1_EPCIS_VERSION,
                                            schema = @Schema(implementation = String.class)),
                                    @Header(
                                            name = "GS1-Extensions",
                                            description = ParameterDescriptions.GS1_EXTENSIONS,
                                            schema = @Schema(implementation = String.class))
                            },
                            content =
                            @Content(
                                    schema = @Schema(anyOf = Object.class),
                                    example =
                                            "{\n"
                                                    + "  \"subscriptionID\": \"7cec849f-72f7-4a9d-bc86-080d3c5b7c98\",\n"
                                                    + "  \"queryName\": \"GetIlmdExtension\",\n"
                                                    + "  \"dest\": \"http://example.com/testWebhookSubscription\",\n"
                                                    + "  \"initialRecordTime\": \"2022-01-06T06:52:24.880Z\",\n"
                                                    + "  \"reportIfEmpty\": true,\n"
                                                    + "  \"schedule\": {\n"
                                                    + "    \"second\": \"0/30\"\n"
                                                    + "  },\n"
                                                    + "  \"createdAt\": \"2022-01-06T06:52:24.889Z\",\n"
                                                    + "  \"lastNotifiedAt\": \"2022-01-08T06:51:21.889Z\"\n"
                                                    + "}\n")),
                    @APIResponse(
                            responseCode = "401",
                            description = "Authorization information is missing or invalid.",
                            content =
                            @Content(
                                    schema = @Schema(implementation = ProblemResponseBody.class),
                                    example = ResponseBodyExamples.RESPONSE_401_UNAUTHORIZED_REQUEST)),
                    @APIResponse(
                            responseCode = "403",
                            description = "Client is unauthorized to access this resource",
                            content =
                            @Content(
                                    schema = @Schema(implementation = ProblemResponseBody.class),
                                    example = ResponseBodyExamples.RESPONSE_403_CLIENT_UNAUTHORIZED)),
                    @APIResponse(
                            responseCode = "404",
                            description = "Resource not found.",
                            content =
                            @Content(
                                    schema = @Schema(implementation = ProblemResponseBody.class),
                                    example = ResponseBodyExamples.RESPONSE_404_RESOURCE_NOT_FOUND)),
                    @APIResponse(
                            responseCode = "406",
                            description = "The server cannot return the response as requested.",
                            content =
                            @Content(
                                    schema = @Schema(implementation = ProblemResponseBody.class),
                                    example = ResponseBodyExamples.RESPONSE_406_NOT_ACCEPTABLE)),
                    @APIResponse(
                            responseCode = "500",
                            description = "This is a server-side problem.",
                            content =
                            @Content(
                                    schema = @Schema(implementation = ProblemResponseBody.class),
                                    example = ResponseBodyExamples.RESPONSE_500_IMPLEMENTATION_EXCEPTION))
            })
    @GET
    @Path("{queryName}/subscriptions/{subscriptionID}")
    @Produces({MediaType.APPLICATION_JSON, "application/problem+json", "application/ld+json"})
    @Consumes({MediaType.APPLICATION_JSON, "application/ld+json"})
    public Uni<Response> queriesSubscriptionIDGet(
            @RestPath
            String subscriptionID,
            @RestPath
            String queryName);

    @Tags(
            value = {
                    @Tag(
                            name = "Queries",
                            description =
                                    "Endpoints to create large named or anonymous queries using the EPCIS Query Language. Named queries have a custom\n"
                                            + "      name and are stored until deleted by the user. Anonymous queries are not persisted and only available to the caller.\n"
                                            + "      EPCIS events queries also support query subscription."),
                    @Tag(name = "Subscriptions", description = "EPCIS 2.0 supports query subscriptions using Webhooks or Websockets.")
            })
    @Operation(
            description = "",
            summary = "Unsubscribes a client by deleting the query subscription.")
    @Parameters(
            value = {
                    @Parameter(description = "", required = true, in = ParameterIn.PATH),
                    @Parameter(description = "", required = true, in = ParameterIn.PATH)
            }
    )
    @APIResponses(
            value = {
                    @APIResponse(responseCode = "204", description = "Client unsubscribed from query."),
                    @APIResponse(
                            responseCode = "401",
                            description = "Authorization information is missing or invalid.",
                            content =
                            @Content(
                                    schema = @Schema(implementation = ProblemResponseBody.class),
                                    example = ResponseBodyExamples.RESPONSE_401_UNAUTHORIZED_REQUEST)),
                    @APIResponse(
                            responseCode = "403",
                            description = "Client is unauthorized to access this resource",
                            content =
                            @Content(
                                    schema = @Schema(implementation = ProblemResponseBody.class),
                                    example = ResponseBodyExamples.RESPONSE_403_CLIENT_UNAUTHORIZED)),
                    @APIResponse(
                            responseCode = "404",
                            description = "Resource not found.",
                            content =
                            @Content(
                                    schema = @Schema(implementation = ProblemResponseBody.class),
                                    example = ResponseBodyExamples.RESPONSE_404_RESOURCE_NOT_FOUND)),
                    @APIResponse(
                            responseCode = "406",
                            description = "The server cannot return the response as requested.",
                            content =
                            @Content(
                                    schema = @Schema(implementation = ProblemResponseBody.class),
                                    example = ResponseBodyExamples.RESPONSE_406_NOT_ACCEPTABLE)),
                    @APIResponse(
                            responseCode = "500",
                            description = "This is a server-side problem.",
                            content =
                            @Content(
                                    schema = @Schema(implementation = ProblemResponseBody.class),
                                    example = ResponseBodyExamples.RESPONSE_500_IMPLEMENTATION_EXCEPTION))
            })
    @DELETE
    @Path("{queryName}/subscriptions/{subscriptionID}")
    @Produces({MediaType.APPLICATION_JSON, "application/problem+json", "application/ld+json"})
    @Consumes({MediaType.APPLICATION_JSON, "application/ld+json"})
    public Uni<Response> queriesSubscriptionIDDelete(@RestPath String subscriptionID);

    @Operation(
            description = "",
            summary =
                    "Optional endpoint that allows on-demand release of any resources associated with `nextPageToken`.")
    @Parameters(
            value = {
                    @Parameter(name = "secret", in = ParameterIn.QUERY, content = @Content(example = "3A1550673"))
            }
    )
    @APIResponses(
            value = {
                    @APIResponse(responseCode = "204", description = "nextPageToken invalidated successfully."),
                    @APIResponse(
                            responseCode = "500",
                            description = "This is a server-side problem.",
                            content =
                            @Content(
                                    schema = @Schema(implementation = ProblemResponseBody.class),
                                    example = ResponseBodyExamples.RESPONSE_500_IMPLEMENTATION_EXCEPTION)),
                    @APIResponse(
                            responseCode = "501",
                            description = "Functionality not supported by server.",
                            content =
                            @Content(
                                    schema = @Schema(implementation = ProblemResponseBody.class),
                                    example = ResponseBodyExamples.RESPONSE_501_NOT_IMPLEMENTED))
            })
    @POST
    @Path("/testWebhookSubscription")
    @Tag(
            name = "Subscriptions",
            description = "EPCIS 2.0 supports query subscriptions using Webhooks or Websockets.")
    public Response testWebhookSubscription(@Valid String events);

    @Tag(name = "Discovery", description = "Endpoints to obtain information about the endpoint, such as EPCIS and CBV versions or custom vocabularies.")
    @Operation(
            summary = "Query the metadata of the subscriptions endpoint.",
            description = "The `OPTIONS` method is used as a discovery service for query subscriptions.")
    @Parameters(
            value = {
                    @Parameter(description = "The name of an EPCIS event query.", required = false, in = ParameterIn.PATH),
                    @Parameter(
                            name = "GS1-Extensions",
                            description = ParameterDescriptions.GS1_EXTENSIONS,
                            in = ParameterIn.HEADER)
            }
    )
    @APIResponses(
            value = {
                    @APIResponse(
                            responseCode = "204",
                            description = "The `OPTIONS` method is used to discover capabilities for EPCIS 2.0 endpoints. It describes which EPCIS and CBV versions are supported and used for the top-level resource as well as EPCIS and CBV extensions. The list of headers is not exhaustive. It only describes the functionality specific to EPCIS 2.0.",
                            headers = {
                                    @Header(
                                            name = "Allow",
                                            description = ParameterDescriptions.ALLOW,
                                            schema = @Schema(implementation = String.class)),
                                    @Header(
                                            name = "GS1-EPCIS-version",
                                            description = ParameterDescriptions.GS1_EPCIS_VERSION,
                                            schema = @Schema(implementation = String.class)),
                                    @Header(
                                            name = "GS1-EPCIS-Min",
                                            description = ParameterDescriptions.GS1_EPCIS_MIN,
                                            schema = @Schema(implementation = String.class)),
                                    @Header(
                                            name = "GS1-EPCIS-Max",
                                            description = ParameterDescriptions.GS1_EPCIS_MAX,
                                            schema = @Schema(implementation = String.class)),
                                    @Header(
                                            name = "GS1-CBV-version",
                                            description = ParameterDescriptions.GS1_CBV_VERSION,
                                            schema = @Schema(implementation = String.class)),
                                    @Header(
                                            name = "GS1-CBV-Min",
                                            description = ParameterDescriptions.GS1_CBV_MIN,
                                            schema = @Schema(implementation = String.class)),
                                    @Header(
                                            name = "GS1-CBV-Max",
                                            description = ParameterDescriptions.GS1_CBV_MAX,
                                            schema = @Schema(implementation = String.class)),
                                    @Header(
                                            name = "GS1-EPC-Format",
                                            description = ParameterDescriptions.GS1_EPC_FORMAT,
                                            schema = @Schema(implementation = String.class)),
                                    @Header(
                                            name = "GS1-CBV-XML-Format",
                                            description = ParameterDescriptions.GS1_CBV_XML_FORMAT,
                                            schema = @Schema(implementation = String.class)),
                                    @Header(
                                            name = "GS1-Extensions",
                                            description = ParameterDescriptions.GS1_EXTENSIONS,
                                            schema = @Schema(implementation = String.class)),
                                    @Header(
                                            name = "GS1-Vendor-Version",
                                            description = ParameterDescriptions.GS1_VENDOR_VERSION,
                                            schema = @Schema(implementation = String.class)),
                            }),
                    @APIResponse(
                            responseCode = "401",
                            description = "Authorization information is missing or invalid.",
                            content =
                            @Content(
                                    schema = @Schema(implementation = ProblemResponseBody.class),
                                    example = ResponseBodyExamples.RESPONSE_401_UNAUTHORIZED_REQUEST)),
                    @APIResponse(
                            responseCode = "403",
                            description = "Client is unauthorized to access this resource.",
                            content =
                            @Content(
                                    schema = @Schema(implementation = ProblemResponseBody.class),
                                    example = ResponseBodyExamples.RESPONSE_403_CLIENT_UNAUTHORIZED)),
                    @APIResponse(
                            responseCode = "406",
                            description = "The server cannot return the response as requested.",
                            content =
                            @Content(
                                    schema = @Schema(implementation = ProblemResponseBody.class),
                                    example = ResponseBodyExamples.RESPONSE_406_NOT_ACCEPTABLE)),
                    @APIResponse(
                            responseCode = "500",
                            description = "This is a server-side problem caused while query execution.",
                            content =
                            @Content(
                                    schema = @Schema(implementation = ProblemResponseBody.class),
                                    example = ResponseBodyExamples.RESPONSE_500_IMPLEMENTATION_EXCEPTION))
            })
    @OPTIONS
    @Path("{queryName}/subscriptions")
    @Produces({MediaType.APPLICATION_JSON, "application/problem+json", "application/ld+json"})
    @Consumes({MediaType.APPLICATION_JSON, "application/ld+json"})
    public Uni<Response> queriesQueryNameSubscriptionOptions();

    @Tag(name = "Discovery", description = "Endpoints to obtain information about the endpoint, such as EPCIS and CBV versions or custom vocabularies.")
    @Operation(
            summary = "Query the metadata of the endpoint for an individual subscription.",
            description = "The `OPTIONS` method is used as a discovery service for query subscriptions.")
    @Parameters(
            value = {
                    @Parameter(description = "The name of an EPCIS event query.", required = false, in = ParameterIn.PATH),
                    @Parameter(
                            name = "GS1-Extensions",
                            description = ParameterDescriptions.GS1_EXTENSIONS,
                            in = ParameterIn.HEADER),
                    @Parameter(description = "", required = false, in = ParameterIn.PATH)
            }
    )
    @APIResponses(
            value = {
                    @APIResponse(
                            responseCode = "204",
                            description = "The `OPTIONS` method is used to discover capabilities for EPCIS 2.0 endpoints. It describes which EPCIS and CBV versions are supported and used for the top-level resource as well as EPCIS and CBV extensions. The list of headers is not exhaustive. It only describes the functionality specific to EPCIS 2.0.",
                            headers = {
                                    @Header(
                                            name = "Allow",
                                            description = ParameterDescriptions.ALLOW,
                                            schema = @Schema(implementation = String.class)),
                                    @Header(
                                            name = "GS1-EPCIS-version",
                                            description = ParameterDescriptions.GS1_EPCIS_VERSION,
                                            schema = @Schema(implementation = String.class)),
                                    @Header(
                                            name = "GS1-EPCIS-Min",
                                            description = ParameterDescriptions.GS1_EPCIS_MIN,
                                            schema = @Schema(implementation = String.class)),
                                    @Header(
                                            name = "GS1-EPCIS-Max",
                                            description = ParameterDescriptions.GS1_EPCIS_MAX,
                                            schema = @Schema(implementation = String.class)),
                                    @Header(
                                            name = "GS1-Extensions",
                                            description = ParameterDescriptions.GS1_EXTENSIONS,
                                            schema = @Schema(implementation = String.class)),
                                    @Header(
                                            name = "GS1-Vendor-Version",
                                            description = ParameterDescriptions.GS1_VENDOR_VERSION,
                                            schema = @Schema(implementation = String.class)),
                                    @Header(
                                            name = "GS1-Query-Min-Record-Time",
                                            description = "Informs about the smallest possible record time for EPCIS events in an outstanding query subscription.",
                                            schema = @Schema(implementation = String.class)),
                            }),
                    @APIResponse(
                            responseCode = "401",
                            description = "Authorization information is missing or invalid.",
                            content =
                            @Content(
                                    schema = @Schema(implementation = ProblemResponseBody.class),
                                    example = ResponseBodyExamples.RESPONSE_401_UNAUTHORIZED_REQUEST)),
                    @APIResponse(
                            responseCode = "403",
                            description = "Client is unauthorized to access this resource.",
                            content =
                            @Content(
                                    schema = @Schema(implementation = ProblemResponseBody.class),
                                    example = ResponseBodyExamples.RESPONSE_403_CLIENT_UNAUTHORIZED)),
                    @APIResponse(
                            responseCode = "404",
                            description = "The EPCIS event does not exist.",
                            content =
                            @Content(
                                    schema = @Schema(implementation = Object.class),
                                    example = ResponseBodyExamples.RESPONSE_404_RESOURCE_NOT_FOUND)),
                    @APIResponse(
                            responseCode = "406",
                            description = "The server cannot return the response as requested.",
                            content =
                            @Content(
                                    schema = @Schema(implementation = ProblemResponseBody.class),
                                    example = ResponseBodyExamples.RESPONSE_406_NOT_ACCEPTABLE)),
                    @APIResponse(
                            responseCode = "500",
                            description = "This is a server-side problem caused while query execution.",
                            content =
                            @Content(
                                    schema = @Schema(implementation = ProblemResponseBody.class),
                                    example = ResponseBodyExamples.RESPONSE_500_IMPLEMENTATION_EXCEPTION))
            })
    @OPTIONS
    @Path("{queryName}/subscriptions/{subscriptionID}")
    @Produces({MediaType.APPLICATION_JSON, "application/problem+json", "application/ld+json"})
    @Consumes({MediaType.APPLICATION_JSON, "application/ld+json"})
    public Uni<Response> queriesSubscriptionIDOptions();

}
