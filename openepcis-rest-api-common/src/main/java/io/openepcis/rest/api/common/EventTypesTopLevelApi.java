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

import io.openepcis.model.rest.ProblemResponseBody;
import io.openepcis.rest.api.common.constants.ParameterDescriptions;
import io.openepcis.rest.api.common.constants.ResponseBodyExamples;
import io.quarkus.security.identity.SecurityIdentity;
import io.smallrye.mutiny.Uni;
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
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import org.jboss.resteasy.reactive.RestPath;
import org.jboss.resteasy.reactive.RestQuery;

import static io.openepcis.rest.api.common.constants.ParameterConstants.*;

@Tag(name = "Top-level Resources", description = "Endpoints to browse or retrieve information about EPCIS resources:\n" +
        "\n" +
        "- events\n" +
        "- types of events\n" +
        "- electronic product codes\n" +
        "- business steps\n" +
        "- business locations\n" +
        "- read points\n" +
        "- dispositions")
@Path("/eventTypes")
@SecurityRequirement(name = "apiKey")
@SecurityRequirement(name = "apiKeySecret")
@SecurityRequirement(name = "bearerAuth")
@SecurityRequirement(name = "oidc")
@RolesAllowed("query")
@RegisterRestClient(configKey = "eventTypes-api")
public interface EventTypesTopLevelApi {

    @Operation(
            summary = "Returns all EPCIS event types currently available in the EPCIS repository",
            description =
                    "EPCIS event types specify the schema of an event. This endpoint returns the 5 standard event types as well as any custom event types supported by this repository.")
    @Parameters(
            value = {
                    @Parameter(
                            name = "perPage",
                            description = ParameterDescriptions.PER_PAGE,
                            example = DEFAULT_PER_PAGE_PARAMETER_VALUE,
                            in = ParameterIn.QUERY,
                            content = @Content(example = "30")),
                    @Parameter(
                            name = "nextPageToken",
                            description = ParameterDescriptions.NEXT_PAGE_TOKEN,
                            in = ParameterIn.QUERY,
                            content = @Content(example = "3A15506738749783AU6D7DENAKwM2gQRRwGritaeq")),
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
                            description = "Returns all supported EPCIS event types.",
                            content =
                            @Content(
                                    schema = @Schema(implementation = Object.class),
                                    example = ResponseBodyExamples.RESPONSE_200_SUPPORTED_EVENT_TYPES)),
                    @APIResponse(
                            responseCode = "400",
                            description = "Query exceptions defined in EPCIS.",
                            content =
                            @Content(
                                    schema = @Schema(implementation = Object.class),
                                    example = ResponseBodyExamples.RESPONSE_400_QUERY_ISSUES)),
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
    @GET
    @Produces({MediaType.APPLICATION_JSON, "application/problem+json", "application/ld+json", "application/xml"})
    @Consumes({MediaType.APPLICATION_JSON, "application/ld+json"})
    Uni<Response> getEventTypes(@Context SecurityIdentity securityIdentity,
                                @Valid
                                @RestQuery
                                @DefaultValue(DEFAULT_PER_PAGE_PARAMETER_VALUE)
                                Integer perPage,
                                @Valid
                                @RestQuery
                                String nextPageToken,
                                @Context UriInfo uriInfo);

    @Operation(
            summary = "Returns all sub-resources of an EPCIS event type.",
            description = "This endpoint returns all sub-resources of an EPCIS event type (for HATEOAS discovery), which includes at least `events`. A server may add additional endpoints, for example `schema` to access the EPCIS event type schema.")
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
                            name = "eventType",
                            description = "Names of EPCIS event types.",
                            required = true,
                            in = ParameterIn.PATH,
                            content = @Content(example = "ObjectEvent"))
            }
    )
    @APIResponses(
            value = {
                    @APIResponse(
                            responseCode = "200",
                            description = "Returns a sub-resource list that contains at least the `events` sub-resource (for HATEOAS discovery).",
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
                            },
                            content =
                            @Content(
                                    schema = @Schema(implementation = Object.class),
                                    example = ResponseBodyExamples.RESPONSE_200_SUPPORTED_EVENT_TYPES)),
                    @APIResponse(
                            responseCode = "400",
                            description = "Query exceptions defined in EPCIS.",
                            content =
                            @Content(
                                    schema = @Schema(implementation = Object.class),
                                    example = ResponseBodyExamples.RESPONSE_400_QUERY_ISSUES)),
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
    @GET
    @Path("/{eventType}")
    @Produces({
            MediaType.APPLICATION_JSON,
            MediaType.APPLICATION_XML,
            MediaType.TEXT_XML,
            "application/problem+json",
            "application/ld+json"
    })
    @Consumes({MediaType.APPLICATION_JSON, "application/ld+json"})
    Uni<Response> getSubResourceOfEventTypes(@Context SecurityIdentity securityIdentity,
                                     @Context UriInfo uriInfo, @RestPath String eventType);
}
