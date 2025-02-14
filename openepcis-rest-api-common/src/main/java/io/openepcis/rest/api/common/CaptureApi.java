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

import com.fasterxml.jackson.core.JsonProcessingException;
import io.openepcis.model.epcis.constants.CommonConstants;
import io.openepcis.model.rest.ProblemResponseBody;
import io.openepcis.rest.api.common.constants.HeaderConstants;
import io.openepcis.rest.api.common.constants.ParameterDescriptions;
import io.openepcis.rest.api.common.constants.ResponseBodyExamples;
import io.openepcis.rest.api.common.filter.EPCISClientRequestFilter;
import io.smallrye.mutiny.Uni;
import jakarta.annotation.security.RolesAllowed;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
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
import org.eclipse.microprofile.openapi.annotations.tags.Tags;
import org.eclipse.microprofile.rest.client.annotation.RegisterProvider;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import java.io.InputStream;

import static io.openepcis.rest.api.common.constants.ParameterConstants.*;

@Tag(name = "Capture", description = "Endpoints to capture EPCIS events in bulk or individually.")
@Path("capture")
@RegisterRestClient(configKey = "epcis-api")
@RegisterProvider(EPCISClientRequestFilter.class)
public interface CaptureApi {
    @Tags(
            value = {
                    @Tag(name = "Capture", description = "Endpoints to capture EPCIS events in bulk or individually."),
                    @Tag(name = "Discovery", description = "Endpoints to obtain information about the endpoint, such as EPCIS and CBV versions or custom vocabularies.")
            })
    @RequestBody(content = { @Content(mediaType = "application/ld+json"), @Content(mediaType = MediaType.APPLICATION_JSON) } )
    @Operation(
            description =
                    "EPCIS 2.0 supports a number of custom headers to describe custom vocabularies and support multiple versions\n"
                            + "        of EPCIS and CBV. The `OPTIONS` method allows the client to discover which vocabularies and EPCIS and CBV\n"
                            + "        versions are used for a given capture job.",
            summary = "Query the metadata of the capture job endpoint.")
    @Parameters(
            value = {
                    @Parameter(description = "", required = false),
                    @Parameter(
                            name = HeaderConstants.GS1_EPCIS_VERSION,
                            description = ParameterDescriptions.GS1_EPCIS_VERSION,
                            in = ParameterIn.HEADER,
                            content = @Content(example = DEFAULT_EPCIS_VERSION_PARAMETER_VALUE)),
                    @Parameter(
                            name = HeaderConstants.GS1_EPCIS_MIN,
                            description = ParameterDescriptions.GS1_EPCIS_MIN,
                            in = ParameterIn.HEADER,
                            content = @Content(example = DEFAULT_EPCIS_MIN_PARAMETER_VALUE)),
                    @Parameter(
                            name = HeaderConstants.GS1_EPCIS_MAX,
                            description = ParameterDescriptions.GS1_EPCIS_MAX,
                            in = ParameterIn.HEADER,
                            content = @Content(example = DEFAULT_EPCIS_MAX_PARAMETER_VALUE)),
                    @Parameter(
                            name = HeaderConstants.GS1_CBV_VERSION,
                            description = ParameterDescriptions.GS1_CBV_VERSION,
                            in = ParameterIn.HEADER,
                            content = @Content(example = DEFAULT_CBV_VERSION_PARAMETER_VALUE)),
                    @Parameter(
                            name = HeaderConstants.GS1_EXTENSIONS,
                            description = ParameterDescriptions.GS1_EXTENSIONS,
                            in = ParameterIn.HEADER)
            }
    )
    @APIResponses(
            value = {
                    @APIResponse(
                            responseCode = "200",
                            description =
                                    "Server can comply with the GS1-EPCIS-related " + "requirements from the client.",
                            content = @Content(schema = @Schema(implementation = Object.class)),
                            headers = {
                                    @Header(
                                            name = HeaderConstants.ALLOW,
                                            description = ParameterDescriptions.ALLOW,
                                            schema = @Schema(implementation = String.class)),
                                    @Header(
                                            name = HeaderConstants.GS1_EPCIS_VERSION,
                                            description = ParameterDescriptions.GS1_EPCIS_VERSION,
                                            schema = @Schema(implementation = String.class)),
                                    @Header(
                                            name = HeaderConstants.GS1_EPCIS_MIN,
                                            description = ParameterDescriptions.GS1_EPCIS_MIN,
                                            schema = @Schema(implementation = String.class)),
                                    @Header(
                                            name = HeaderConstants.GS1_EPCIS_MAX,
                                            description = ParameterDescriptions.GS1_EPCIS_MAX,
                                            schema = @Schema(implementation = String.class)),
                                    @Header(
                                            name = HeaderConstants.GS1_CBV_VERSION,
                                            description = ParameterDescriptions.GS1_CBV_VERSION,
                                            schema = @Schema(implementation = String.class)),
                                    @Header(
                                            name = HeaderConstants.GS1_CBV_MIN,
                                            description = ParameterDescriptions.GS1_CBV_MIN,
                                            schema = @Schema(implementation = String.class)),
                                    @Header(
                                            name = HeaderConstants.GS1_CBV_MAX,
                                            description = ParameterDescriptions.GS1_CBV_MAX,
                                            schema = @Schema(implementation = String.class)),
                                    @Header(
                                            name = CommonConstants.GS1_EPC_FORMAT,
                                            description = ParameterDescriptions.GS1_EPC_FORMAT,
                                            schema = @Schema(implementation = String.class)),
                                    @Header(
                                            name = HeaderConstants.GS1_CBV_XML_FORMAT,
                                            description = ParameterDescriptions.GS1_CBV_XML_FORMAT,
                                            schema = @Schema(implementation = String.class)),
                                    @Header(
                                            name = HeaderConstants.GS1_EXTENSIONS,
                                            description = ParameterDescriptions.GS1_EXTENSIONS,
                                            schema = @Schema(implementation = Object.class)),
                                    @Header(
                                            name = HeaderConstants.GS1_VENDOR_VERSION,
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
                            description = "Client is unauthorized to access this resource.",
                            content =
                            @Content(
                                    schema = @Schema(implementation = ProblemResponseBody.class),
                                    example = ResponseBodyExamples.RESPONSE_403_CLIENT_UNAUTHORIZED)),
                    @APIResponse(
                            responseCode = "404",
                            description = "At least one resource was not found. For example, the EPCIS event does not exist or the query does not exist.",
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
                            description = "An error occurred on the backend.",
                            content =
                            @Content(
                                    schema = @Schema(implementation = ProblemResponseBody.class),
                                    example = ResponseBodyExamples.RESPONSE_500_IMPLEMENTATION_EXCEPTION))
            })
    @OPTIONS
    @Path("{captureID}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML,MediaType.TEXT_XML, "application/ld+json"})
    public Response captureCaptureIDOptions();


  @Tags(
            value = {
                    @Tag(name = "Capture", description = "Endpoints to capture EPCIS events in bulk or individually."),
                    @Tag(name = "Discovery", description = "Endpoints to obtain information about the endpoint, such as EPCIS and CBV versions or custom vocabularies.")
            })
    @Operation(
            description =
                    "The `OPTIONS` method is used as a discovery service for `/capture`. It describes\n"
                            + "- which EPCIS and CBV versions are supported,\n"
                            + "- the EPCIS and CBV extensions,\n"
                            + "- the maximum payload size as count of EPCIS events (`GS1-EPCIS-Capture-Limit` header) or as a maximum payload size in bytes (`GS1-EPCIS-Capture-File-Size-Limit` header)\n"
                            + "- what the server will do if an error occurred during capture (`GS1-Capture-Error-Behaviour` header)."
                            + "The list of headers is not exhaustive. It only describes the functionality specific to EPCIS 2.0.",
            summary = "Discover the settings of the capture interface.")
    @Parameters(
            value = {
                    @Parameter(
                            name = HeaderConstants.GS1_EPCIS_VERSION,
                            description = ParameterDescriptions.GS1_EPCIS_VERSION,
                            in = ParameterIn.HEADER,
                            content = @Content(example = DEFAULT_EPCIS_VERSION_PARAMETER_VALUE)),
                    @Parameter(
                            name = HeaderConstants.GS1_EPCIS_MIN,
                            description = ParameterDescriptions.GS1_EPCIS_MIN,
                            in = ParameterIn.HEADER,
                            content = @Content(example = DEFAULT_EPCIS_MIN_PARAMETER_VALUE)),
                    @Parameter(
                            name = HeaderConstants.GS1_EPCIS_MAX,
                            description = ParameterDescriptions.GS1_EPCIS_MAX,
                            in = ParameterIn.HEADER,
                            content = @Content(example = DEFAULT_EPCIS_MAX_PARAMETER_VALUE)),
                    @Parameter(
                            name = HeaderConstants.GS1_CBV_VERSION,
                            description = ParameterDescriptions.GS1_CBV_VERSION,
                            in = ParameterIn.HEADER,
                            content = @Content(example = DEFAULT_CBV_VERSION_PARAMETER_VALUE)),
                    @Parameter(
                            name = HeaderConstants.GS1_EXTENSIONS,
                            description = ParameterDescriptions.GS1_EXTENSIONS,
                            in = ParameterIn.HEADER),
                    @Parameter(
                            name = HeaderConstants.GS1_EPCIS_CAPTURE_LIMIT,
                            description = ParameterDescriptions.GS1_EPCIS_CAPTURE_LIMIT,
                            in = ParameterIn.HEADER),
                    @Parameter(
                            name = HeaderConstants.GS1_CAPTURE_FILE_SIZE_LIMIT,
                            description = ParameterDescriptions.GS1_EPCIS_CAPTURE_FILE_SIZE_LIMIT,
                            in = ParameterIn.HEADER)
            }
    )
    @APIResponses(
            value = {
                    @APIResponse(
                            responseCode = "204",
                            description =
                                    "The `OPTIONS` method is used to discover capabilities for EPCIS 2.0 endpoints."
                                            + "It describes which EPCIS and CBV versions are supported and used for the top-level resource as well as EPCIS and"
                                            + "CBV extensions. The list of headers is not exhaustive. It only describes the functionality specific to EPCIS 2.0.",
                            content = @Content(schema = @Schema(implementation = Object.class)),
                            headers = {
                                    @Header(
                                            name = HeaderConstants.ALLOW,
                                            description = ParameterDescriptions.ALLOW,
                                            schema = @Schema(implementation = String.class)),
                                    @Header(
                                            name = HeaderConstants.GS1_EPCIS_VERSION,
                                            description = ParameterDescriptions.GS1_EPCIS_VERSION,
                                            schema = @Schema(implementation = String.class)),
                                    @Header(
                                            name = HeaderConstants.GS1_EPCIS_MIN,
                                            description = ParameterDescriptions.GS1_EPCIS_MIN,
                                            schema = @Schema(implementation = String.class)),
                                    @Header(
                                            name = HeaderConstants.GS1_EPCIS_MAX,
                                            description = ParameterDescriptions.GS1_EPCIS_MAX,
                                            schema = @Schema(implementation = String.class)),
                                    @Header(
                                            name = HeaderConstants.GS1_CBV_VERSION,
                                            description = ParameterDescriptions.GS1_CBV_VERSION,
                                            schema = @Schema(implementation = String.class)),
                                    @Header(
                                            name = HeaderConstants.GS1_CBV_MIN,
                                            description = ParameterDescriptions.GS1_CBV_MIN,
                                            schema = @Schema(implementation = String.class)),
                                    @Header(
                                            name = HeaderConstants.GS1_CBV_MAX,
                                            description = ParameterDescriptions.GS1_CBV_MAX,
                                            schema = @Schema(implementation = String.class)),
                                    @Header(
                                            name = HeaderConstants.GS1_EPC_FORMAT,
                                            description = ParameterDescriptions.GS1_EPC_FORMAT,
                                            schema = @Schema(implementation = String.class)),
                                    @Header(
                                            name = HeaderConstants.GS1_CBV_XML_FORMAT,
                                            description = ParameterDescriptions.GS1_CBV_XML_FORMAT,
                                            schema = @Schema(implementation = String.class)),
                                    @Header(
                                            name = HeaderConstants.GS1_EXTENSIONS,
                                            description = ParameterDescriptions.GS1_EXTENSIONS,
                                            schema = @Schema(implementation = Object.class)),
                                    @Header(
                                            name = HeaderConstants.GS1_VENDOR_VERSION,
                                            description = ParameterDescriptions.GS1_VENDOR_VERSION,
                                            schema = @Schema(implementation = Object.class)),
                                    @Header(
                                            name = HeaderConstants.GS1_EPCIS_CAPTURE_LIMIT,
                                            description = ParameterDescriptions.GS1_EPCIS_CAPTURE_LIMIT,
                                            schema = @Schema(implementation = String.class)),
                                    @Header(
                                            name = HeaderConstants.GS1_CAPTURE_FILE_SIZE_LIMIT,
                                            description = ParameterDescriptions.GS1_EPCIS_CAPTURE_FILE_SIZE_LIMIT,
                                            schema = @Schema(implementation = String.class)),
                                    @Header(
                                            name = HeaderConstants.GS1_CAPTURE_ERROR_BEHAVIOUR,
                                            description = ParameterDescriptions.GS1_CAPTURE_ERROR_BEHAVIOUR,
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
                                    example = ResponseBodyExamples.RESPONSE_500_IMPLEMENTATION_EXCEPTION))
            })
    @OPTIONS
    @Produces({MediaType.APPLICATION_XML, MediaType.TEXT_XML, MediaType.APPLICATION_JSON, "application/ld+json"})
    @Consumes(MediaType.WILDCARD)
    public Uni<Response> captureOptions();

  /**
   * additional methods required to support client specific consume annotation
   */
  @OPTIONS
  @Produces({MediaType.APPLICATION_XML, MediaType.TEXT_XML, MediaType.APPLICATION_JSON, "application/ld+json"})
  @Consumes(MediaType.APPLICATION_JSON)
  @Operation(hidden = true)
  public Uni<Response> captureOptionsJson();

  @OPTIONS
  @Produces({MediaType.APPLICATION_XML, MediaType.TEXT_XML, MediaType.APPLICATION_JSON, "application/ld+json"})
  @Consumes("application/ld+json")
  @Operation(hidden = true)
  public Uni<Response> captureOptionsJsonLD();

    @Operation(
            summary = "Asynchronous capture interface for one or more EPCIS events.",
            description =
                    "EPCIS events are added in bulk using the capture interface. Four design considerations were made to remain compatible with EPCIS 1.2:\n"
                            + "- EPCIS 2.0 keeps event IDs optional. If event IDs are missing, the server should populate the event ID with a unique value. Otherwise, it won't be possible to retrieve these events by eventID.\n"
                            + "- By default, EPCIS events are only stored if the entire capture job was successful. This behaviour can be changed with the `GS1-Capture-Error-Behaviour` header.\n"
                            + "- EPCIS master data can be captured in the header (`epcisHeader`) of an `EPCISDocument`.\n"
                            + "- This endpoint should support both `EPCISDocument` and `EPCISQueryDocument` as input."
                            + "To prevent timeouts for large payloads, the client potentially may need to split the payload into several capture calls. To that end, the server can specify a capture"
                            + "limit (number of EPCIS events) and file size limit (payload size).A successful capturing of events does not guarantee that events will be stored. Instead, the server returns a"
                            + "capture id, which the client can use to obtain information about the capture job.")
    @Parameters(
            value = {
                    @Parameter(
                            name = HeaderConstants.GS1_CAPTURE_ERROR_BEHAVIOUR,
                            description = ParameterDescriptions.GS1_CAPTURE_ERROR_BEHAVIOUR,
                            in = ParameterIn.HEADER,
                            content = @Content(example = "rollback")),
                    @Parameter(
                            name = HeaderConstants.GS1_EPCIS_VERSION,
                            description = ParameterDescriptions.GS1_EPCIS_VERSION,
                            in = ParameterIn.HEADER,
                            content = @Content(example = DEFAULT_EPCIS_VERSION_PARAMETER_VALUE)),
                    @Parameter(
                            name = HeaderConstants.GS1_CBV_VERSION,
                            description = ParameterDescriptions.GS1_CBV_VERSION,
                            in = ParameterIn.HEADER,
                            content = @Content(example = DEFAULT_CBV_VERSION_PARAMETER_VALUE)),
                    @Parameter(
                            name = HeaderConstants.GS1_EXTENSIONS,
                            description = ParameterDescriptions.GS1_EXTENSIONS,
                            in = ParameterIn.HEADER),
                    @Parameter(
                            name = HeaderConstants.ROLES_ALLOWED,
                            description = ParameterDescriptions.ROLES_ALLOWED,
                            in = ParameterIn.HEADER)
            }
    )
    @APIResponses(
            value = {
                    @APIResponse(
                            responseCode = "202",
                            description =
                                    "Successfully received one or more EPCIS events. The "
                                            + "request returns a unique capture job URL in the Location header.",
                            headers = {
                                    @Header(
                                            name = HeaderConstants.GS1_EPCIS_VERSION,
                                            description = ParameterDescriptions.GS1_EPCIS_VERSION,
                                            schema = @Schema(implementation = String.class)),
                                    @Header(
                                            name = HeaderConstants.GS1_CBV_VERSION,
                                            description = ParameterDescriptions.GS1_CBV_VERSION,
                                            schema = @Schema(implementation = String.class)),
                                    @Header(
                                            name = HeaderConstants.GS1_EXTENSIONS,
                                            description = ParameterDescriptions.GS1_EXTENSIONS,
                                            schema = @Schema(implementation = String.class)),
                                    @Header(
                                            name = "Location",
                                            description = ParameterDescriptions.LOCATION_CAPTURE,
                                            schema = @Schema(implementation = String.class))
                            }),
                    @APIResponse(
                            responseCode = "400",
                            description =
                                    "An error occurred while receiving EPCIS events. All "
                                            + "events are rejected. This is not to be confused with an error while capturing EPCIS events. To "
                                            + "monitor the capture job, use the `/capture/{captureID}` endpoint.",
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
                            responseCode = "413",
                            description =
                                    "The `POST` request is too large. It exceeds the limits "
                                            + "set in `GS1-EPCIS-Capture-Limit` and/or `GS1-EPCIS-Capture-File-Size-Limit`.",
                            content =
                            @Content(
                                    schema = @Schema(implementation = ProblemResponseBody.class),
                                    example = ResponseBodyExamples.RESPONSE_413_CAPTURE_PAYLOAD_TOO_LARGE)),
                    @APIResponse(
                            responseCode = "415",
                            description =
                                    "The client sent data in a format that is not supported " + "by the server.",
                            content =
                            @Content(
                                    schema = @Schema(implementation = ProblemResponseBody.class),
                                    example = ResponseBodyExamples.RESPONSE_415_UNSUPPORTED_MEDIA_TYPE)),
                    @APIResponse(
                            responseCode = "500",
                            description = "An error occurred on the backend.",
                            content =
                            @Content(
                                    schema = @Schema(implementation = ProblemResponseBody.class),
                                    example = ResponseBodyExamples.RESPONSE_500_IMPLEMENTATION_EXCEPTION))
            })
    @POST
    @Produces({"application/problem+json", "application/ld+json"})
    @Consumes(MediaType.APPLICATION_JSON)
    public Uni<Response> capturePost(
            @Valid InputStream body)
            throws JsonProcessingException;

  @POST
  @Produces({"application/problem+json", "application/ld+json"})
  @Consumes(MediaType.APPLICATION_XML)
  @Operation(hidden = true)
  public Uni<Response> capturePostXML(
          @Valid InputStream body)
          throws JsonProcessingException;

  @POST
  @Produces({"application/problem+json", "application/ld+json"})
  @Consumes(MediaType.TEXT_XML)
  @Operation(hidden = true)
  public Uni<Response> capturePostTextXML(
          @Valid InputStream body)
          throws JsonProcessingException;

  @POST
  @Produces({"application/problem+json", "application/ld+json"})
  @Consumes("application/ld+json")
  @Operation(hidden = true)
  public Uni<Response> capturePostJsonLD(
          @Valid InputStream body)
          throws JsonProcessingException;

}
