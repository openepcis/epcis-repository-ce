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

import io.openepcis.model.epcis.EPCISEvent;
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
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import java.io.IOException;
import java.io.InputStream;

import static io.openepcis.rest.api.common.constants.ParameterConstants.*;

@SecurityRequirement(name = "apiKey")
@SecurityRequirement(name = "apiKeySecret")
@SecurityRequirement(name = "bearerAuth")
@SecurityRequirement(name = "oidc")
@RolesAllowed("query") // security annotation not being inherited from interface
@Path("/")
@RegisterRestClient(configKey = "events-capture-api")
public interface EventsCaptureApi {

    @Operation(summary = "The Events Validation API", description = "eventsValidate")
    @Tag(
            name = "Events",
            description = "Endpoints that allow you to retrieve EPCIS events as Web resources.")
    @Parameters(
            value = {
                    @Parameter(
                            name = "GS1-EPCIS-Version",
                            description = ParameterDescriptions.GS1_EPCIS_VERSION,
                            in = ParameterIn.HEADER,
                            content = @Content(example = DEFAULT_EPCIS_VERSION_PARAMETER_VALUE)),
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
                            name = "GS1-CBV-Version",
                            description = ParameterDescriptions.GS1_CBV_VERSION,
                            in = ParameterIn.HEADER,
                            content = @Content(example = DEFAULT_CBV_VERSION_PARAMETER_VALUE))
            }
    )
    @APIResponses(
            value = {
                    @APIResponse(
                            responseCode = "200",
                            description = "Successfully validated one or more EPCIS events"),
                    @APIResponse(
                            responseCode = "400",
                            description = "An error occurred while validating EPCIS events",
                            content =
                            @Content(
                                    schema = @Schema(implementation = ProblemResponseBody.class),
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
                            description = "An error occurred on the backend.",
                            content =
                            @Content(
                                    schema = @Schema(implementation = ProblemResponseBody.class),
                                    example = ResponseBodyExamples.RESPONSE_500_IMPLEMENTATION_EXCEPTION)),
                    @APIResponse(
                            responseCode = "415",
                            description =
                                    "The client sent data in a format that is not supported " + "by the server.",
                            content =
                            @Content(
                                    schema = @Schema(implementation = ProblemResponseBody.class),
                                    example = ResponseBodyExamples.RESPONSE_415_UNSUPPORTED_MEDIA_TYPE)),
                    @APIResponse(
                            responseCode = "413",
                            description =
                                    "The `POST` request is too large. It exceeds the limits "
                                            + "set in `GS1-EPCIS-Capture-Limit` and/or `GS1-EPCIS-Capture-File-Size-Limit`.",
                            content =
                            @Content(
                                    schema = @Schema(implementation = ProblemResponseBody.class),
                                    example = ResponseBodyExamples.RESPONSE_413_CAPTURE_PAYLOAD_TOO_LARGE))
            })
    @POST
    @Path("events/validate")
    @Produces({
            MediaType.APPLICATION_JSON,
            MediaType.APPLICATION_XML,
            MediaType.TEXT_XML,
            "application/problem+json",
            "application/ld+json"
    })
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, "application/ld+json"})
    public Uni<Response> validateEvents(
            @Context SecurityIdentity securityIdentity,
            @Valid InputStream body)
            throws IOException;

    @Operation(
            summary = "Synchronous capture endpoint for a single EPCIS event.",
            description =
                    "An individual EPCIS event can be created by making a `POST` request on the `/events` resource. Alternatively, the client can also use the `/capture`"
                            + "interface and capture a single event.")
    @Parameters(
            value = {
                    @Parameter(
                            name = "GS1-EPCIS-Version",
                            description = ParameterDescriptions.GS1_EPCIS_VERSION,
                            in = ParameterIn.HEADER,
                            content = @Content(example = DEFAULT_EPCIS_VERSION_PARAMETER_VALUE)),
                    @Parameter(
                            name = "GS1-CBV-Version",
                            description = ParameterDescriptions.GS1_CBV_VERSION,
                            in = ParameterIn.HEADER,
                            content = @Content(example = DEFAULT_CBV_VERSION_PARAMETER_VALUE)),
                    @Parameter(
                            name = "GS1-Extensions",
                            description = ParameterDescriptions.GS1_EXTENSIONS,
                            in = ParameterIn.HEADER),
                    @Parameter(
                            name = "Roles-Allowed",
                            description = ParameterDescriptions.ROLES_ALLOWED,
                            in = ParameterIn.HEADER)
            }
    )
    @APIResponses(
            value = {
                    @APIResponse(
                            responseCode = "201",
                            description =
                                    "Successfully created (captured) the EPCIS event. The request returns Event for given EventId",
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
                                            description = "Absolute or relative URL of the newly captured EPCIS event.",
                                            schema = @Schema(implementation = String.class))
                            },
                            content =
                            @Content(
                                    schema = @Schema(implementation = EPCISEvent.class),
                                    example = ResponseBodyExamples.RESPONSE_201_EPCIS_BARE_EVENT)),
                    @APIResponse(
                            responseCode = "400",
                            description =
                                    "An error occurred while creating the EPCIS event. The event was rejected.",
                            content =
                            @Content(
                                    schema = @Schema(implementation = ProblemResponseBody.class),
                                    example = ResponseBodyExamples.RESPONSE_400_VALIDATION_EXCEPTION)),
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
                            description = "Event with same identifier already exists.",
                            content =
                            @Content(
                                    schema = @Schema(implementation = ProblemResponseBody.class),
                                    example = ResponseBodyExamples.RESPONSE_409_RESOURCE_ALREADY_EXISTS_EXCEPTION)),
                    @APIResponse(
                            responseCode = "413",
                            description =
                                    "The POST request is too large. It exceeds the limits set in GS1-EPCIS-Capture-Limit and/or GS1-EPCIS-Capture-File-Size-Limit.",
                            headers = {
                                    @Header(
                                            name = "GS1-EPCIS-Capture-Limit",
                                            description = ParameterDescriptions.GS1_EPCIS_CAPTURE_LIMIT,
                                            schema = @Schema(implementation = String.class)),
                                    @Header(
                                            name = "GS1-EPCIS-Capture-File-Size-Limit",
                                            description = ParameterDescriptions.GS1_EPCIS_CAPTURE_FILE_SIZE_LIMIT,
                                            schema = @Schema(implementation = String.class))
                            },
                            content =
                            @Content(
                                    schema = @Schema(implementation = ProblemResponseBody.class),
                                    example = ResponseBodyExamples.RESPONSE_413_CAPTURE_PAYLOAD_TOO_LARGE)),
                    @APIResponse(
                            responseCode = "415",
                            description = "The client sent data in a format that is not supported by the server.",
                            content =
                            @Content(
                                    schema = @Schema(implementation = ProblemResponseBody.class),
                                    example = ResponseBodyExamples.RESPONSE_415_UNSUPPORTED_MEDIA_TYPE)),
                    @APIResponse(
                            responseCode = "500",
                            description = "A server-side error occurred",
                            content =
                            @Content(
                                    schema = @Schema(implementation = ProblemResponseBody.class),
                                    example = ResponseBodyExamples.RESPONSE_500_IMPLEMENTATION_EXCEPTION))
            })
    @POST
    @Path("/events")
    @Tag(name = "Capture", description = "Endpoints to capture EPCIS events in bulk or individually.")
    @Produces({
            MediaType.APPLICATION_JSON,
            MediaType.APPLICATION_XML,
            MediaType.TEXT_XML,
            "application/problem+json",
            "application/ld+json"
    })
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, "application/ld+json"})
    @RequestBody(
            required = true,
            content = @Content(schema = @Schema(implementation = EPCISEvent.class)))
    public Uni<Response> postEvent(
            @Context SecurityIdentity securityIdentity,
            @Valid String epcisDocumentStr);
}
