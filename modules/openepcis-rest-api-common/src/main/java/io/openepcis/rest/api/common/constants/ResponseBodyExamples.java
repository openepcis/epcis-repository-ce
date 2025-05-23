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
package io.openepcis.rest.api.common.constants;

public interface ResponseBodyExamples {
  String RESPONSE_200_TOP_LEVEL_SUB_RESOURCE_ENDPOINT =
          "{ \n"
                  + "  \"@context\": \"https://ref.gs1.org/standards/epcis/2.0.0/epcis-context.jsonld\",\n"
                  + "  \"type\": \"Collection\",\n"
                  + "  \"member\": [\"events\"]\n"
                  + "}";
  String RESPONSE_200_EPCIS_QUERY_DOCUMENT =
          "{\n"
                  + "  \"$ref\": \"https://ref.gs1.org/docs/epcis/examples/epcis_query_document.jsonld\"\n"
                  + "}";
  String RESPONSE_200_EPCIS_QUERY_DOCUMENT_SINGLE_PAGE =
          "{\n"
                  + "  \"$ref\": \"https://ref.gs1.org/docs/epcis/examples/epcis_query_document.jsonld\"\n"
                  + "}";
  String RESPONSE_201_EPCIS_BARE_EVENT =
          "{\n"
                  + "  \"$ref\": \"https://ref.gs1.org/docs/epcis/examples/example_9.6.2-object_event.jsonld\"\n"
                  + "}";
  String RESPONSE_200_SUPPORTED_TOP_LEVEL =
          "{\n"
                  + "  \"@context\": \"https://ref.gs1.org/standards/epcis/2.0.0/epcis-context.jsonld\",\n"
                  + "  \"type\": \"Collection\",\n"
                  + "  \"member\": [\n"
                  + "    \"queries\",\n"
                  + "    \"capture\",\n"
                  + "    \"events\",\n"
                  + "    \"eventTypes\",\n"
                  + "    \"epcs\", \n"
                  + "    \"readPoints\",\n"
                  + "    \"bizLocations\",\n"
                  + "    \"dispositions\",\n"
                  + "    \"bizSteps\"\n"
                  + "  ]\n"
                  + "}";
  String RESPONSE_200_SUPPORTED_EVENT_TYPES =
          "{\n"
                  + "  \"@context\": [\n"
                  + "    \"https://ref.gs1.org/standards/epcis/2.0.0/epcis-context.jsonld\",\n"
                  + "    {\n"
                  + "      \"ex\": \"https://example.org/myCustomEventTypes/\"\n"
                  + "    }\n"
                  + "  ],\n"
                  + "  \"type\": \"Collection\",\n"
                  + "  \"member\": [\n"
                  + "    \"AggregationEvent\",\n"
                  + "    \"AssociationEvent\",\n"
                  + "    \"ObjectEvent\",\n"
                  + "    \"TransactionEvent\",\n"
                  + "    \"TransformationEvent\",\n"
                  + "    \"ex:aCustomEventType\"\n"
                  + "  ]\n"
                  + "}";
  String RESPONSE_400_SUBSCRIPTION_ISSUES =
          "{\n"
                  + "  \"type\": \"epcisException:SubscriptionControlsException\",\n"
                  + "  \"title\": \"Subscription error\",\n"
                  + "  \"status\": 400\n"
                  + "}";
  String RESPONSE_400_QUERY_ISSUES =
          "{\n"
                  + "  \"type\": \"epcisException:QueryValidationException\",\n"
                  + "  \"title\": \"EPCIS query exception\",\n"
                  + "  \"status\": 400\n"
                  + "}";
  String RESPONSE_400_VALIDATION_EXCEPTION =
          "{\n"
                  + "  \"status\": 400,\n"
                  + "  \"type\": \"epcisException:ValidationException\",\n"
                  + "  \"title\": \"string\",\n"
                  + "  \"detail\": \"string\",\n"
                  + "  \"instance\": \"string\"\n"
                  + "}";
  String RESPONSE_401_UNAUTHORIZED_REQUEST =
          "{\n"
                  + "  \"type\": \"epcisException:SecurityException\",\n"
                  + "  \"title\": \"Unauthorised request\",\n"
                  + "  \"status\": 401\n"
                  + "}";
  String RESPONSE_403_CLIENT_UNAUTHORIZED =
          "{\n"
                  + "  \"type\": \"epcisException:SecurityException\",\n"
                  + "  \"title\": \"Access to resource forbidden\",\n"
                  + "  \"status\": 403\n"
                  + "}";
  String RESPONSE_404_RESOURCE_NOT_FOUND =
          "{\n"
                  + "  \"type\": \"epcisException:NoSuchResourceException\",\n"
                  + "  \"title\": \"Resource not found\",\n"
                  + "  \"status\": 404\n"
                  + "}";
  String RESPONSE_413_CAPTURE_PAYLOAD_TOO_LARGE =
          "{\n"
                  + "  \"type\": \"epcisException:CaptureLimitExceededException\",\n"
                  + "  \"title\": \"Capture Payload too large\",\n"
                  + "  \"status\": 413\n"
                  + "}";
  String RESPONSE_413_QUERY_SCOPE_OR_SIZE =
          "{\n"
                  + "  \"type\": \"epcisException:QueryTooComplexException\",\n"
                  + "  \"title\": \"Capture Payload too large\",\n"
                  + "  \"status\": 413\n"
                  + "}";
  String RESPONSE_414_URL_TOO_LONG =
          "{\n"
                  + "  \"type\": \"epcisException:URITooLongException\",\n"
                  + "  \"title\": \"URI Too Long\",\n"
                  + "  \"status\": 414\n"
                  + "}";
  String RESPONSE_406_NOT_ACCEPTABLE =
          "{\n"
                  + "  \"type\": \"epcisException:NotAcceptableException\",\n"
                  + "  \"title\": \"Conflicting request and response headers\",\n"
                  + "  \"status\": 406\n"
                  + "}";
  String RESPONSE_409_RESOURCE_ALREADY_EXISTS_EXCEPTION =
          "{\n"
                  + "  \"type\": \"epcisException:ResourceAlreadyExistsException\",\n"
                  + "  \"title\": \"A resource with the provided identifier already exists.\",\n"
                  + "  \"status\": 409\n"
                  + "}";
  String RESPONSE_415_UNSUPPORTED_MEDIA_TYPE =
          "{\n"
                  + "  \"type\": \"epcisException:UnsupportedMediaTypeException\",\n"
                  + "  \"title\": \"Unsupported Media Type\",\n"
                  + "  \"status\": 415\n"
                  + "}";
  String RESPONSE_500_IMPLEMENTATION_EXCEPTION =
          "{\n"
                  + "  \"type\": \"epcisException:ImplementationException\",\n"
                  + "  \"title\": \"A server-side error occurred\",\n"
                  + "  \"status\": 500\n"
                  + "}";
  String RESPONSE_501_NOT_IMPLEMENTED =
          "{\n"
                  + "  \"type\": \"epcisException:ImplementationException\",\n"
                  + "  \"title\": \"Functionality not supported by server\",\n"
                  + "  \"status\": 501\n"
                  + "}";
  String RESPONSE_200_TOP_LEVEL_OR_BIZLOCATION_ENDPOINT =
          "{ \n"
                  + "  \"@context\": \"https://ref.gs1.org/standards/epcis/2.0.0/epcis-context.jsonld\",\n"
                  + "  \"type\": \"Collection\",\n"
                  + "  \"member\": [\"urn:epc:id:sgln:9524987.20000.0\"]\n"
                  + "}";
  String RESPONSE_200_TOP_LEVEL_OR_READPOINT_ENDPOINT =
          "{ \n"
                  + "  \"@context\": \"https://ref.gs1.org/standards/epcis/2.0.0/epcis-context.jsonld\",\n"
                  + "  \"type\": \"Collection\",\n"
                  + "  \"member\": [\n" +
                  "\"urn:epc:id:sgln:9524678.90000.WarehouseD2\",\n" +
                  "\"urn:epc:id:sgln:9524678.90000.WarehouseD1\"]\n"
                  + "}";
  String RESPONSE_200_TOP_LEVEL_OR_EPC_ENDPOINT =
          "{ \n"
                  + "  \"@context\": \"https://ref.gs1.org/standards/epcis/2.0.0/epcis-context.jsonld\",\n"
                  + "  \"type\": \"Collection\",\n"
                  + "  \"member\": [\n" +
                  "\"urn:epc:id:sgtin:0614141.107346.2018\",\n" +
                  "\"urn:epc:id:sgtin:0614141.107346.2017 \"]\n"
                  + "}";
  String RESPONSE_200_SUPPORTED_BIZSTEPS =
          "{\n"
                  + "  \"@context\": [\n"
                  + "    \"https://ref.gs1.org/standards/epcis/2.0.0/epcis-context.jsonld\",\n"
                  + "    {\n"
                  + "      \"ex\": \"https://example.org/myCustomEventTypes/\"\n"
                  + "    }\n"
                  + "  ],\n"
                  + "  \"type\": \"Collection\",\n"
                  + "  \"member\": [\n"
                  + "    \"arriving\",\n"
                  + "    \"assembling\",\n"
                  + "    \"collecting\",\n"
                  + "    \"commissioning\",\n"
                  + "    \"consigning\",\n"
                  + "    \"creating_class_instance\"\n"
                  + "    \"cycle_counting\",\n"
                  + "    \"decommissioning\",\n"
                  + "    \"departing\",\n"
                  + "    \"destroying\",\n"
                  + "    \"disassembling\",\n"
                  + "    \"dispensing\"\n"
                  + "    \"encoding\",\n"
                  + "    \"assembling\",\n"
                  + "    \"entering_exiting\",\n"
                  + "    \"holding\",\n"
                  + "    \"inspecting\",\n"
                  + "    \"installing\"\n"
                  + "    \"killing\",\n"
                  + "    \"loading\",\n"
                  + "    \"other\",\n"
                  + "    \"packing\",\n"
                  + "    \"picking\",\n"
                  + "    \"receiving\"\n"
                  + "    \"removing\",\n"
                  + "    \"repackaging\",\n"
                  + "    \"repairing\",\n"
                  + "    \"replacing\",\n"
                  + "    \"reserving\"\n"
                  + "    \"retail_selling\",\n"
                  + "    \"sampling\",\n"
                  + "    \"sensor_reporting\",\n"
                  + "    \"shipping\",\n"
                  + "    \"staging_outbound\",\n"
                  + "    \"stock_taking\"\n"
                  + "    \"stocking\",\n"
                  + "    \"storing\"\n"
                  + "    \"transporting\",\n"
                  + "    \"unloading\",\n"
                  + "    \"unpacking\",\n"
                  + "    \"void_shipping\",\n"
                  + "    \"ex:aCustomBizStep\"\n"
                  + "  ]\n"
                  + "}";
  String RESPONSE_200_SUPPORTED_DISPOSITIONS =
          "{\n"
                  + "  \"@context\": [\n"
                  + "    \"https://ref.gs1.org/standards/epcis/2.0.0/epcis-context.jsonld\",\n"
                  + "  ],\n"
                  + "  \"type\": \"Collection\",\n"
                  + "  \"member\": [\n"
                  + "    \"active\",\n"
                  + "    \"available\",\n"
                  + "    \"completeness_inferred\",\n"
                  + "    \"completeness_verified\",\n"
                  + "    \"conformant\",\n"
                  + "    \"container_closed\",\n"
                  + "    \"container_open\",\n"
                  + "    \"damaged\",\n"
                  + "    \"destroyed\",\n"
                  + "    \"dispensed\",\n"
                  + "    \"disposed\",\n"
                  + "    \"encoded\"\n"
                  + "    \"expired\",\n"
                  + "    \"in_progress\",\n"
                  + "    \"in_transit\",\n"
                  + "    \"inactive\",\n"
                  + "    \"mismatch_class\",\n"
                  + "    \"mismatch_instance\",\n"
                  + "    \"mismatch_quantity\",\n"
                  + "    \"needs_replacement\",\n"
                  + "    \"no_pedigree_match\",\n"
                  + "    \"non_conformant\",\n"
                  + "    \"non_sellable_other\",\n"
                  + "    \"partially_dispensed\"\n"
                  + "    \"recalled\",\n"
                  + "    \"reserved\",\n"
                  + "    \"retail_sold\",\n"
                  + "    \"returned\",\n"
                  + "    \"sellable_accessible\",\n"
                  + "    \"sellable_not_accessible\",\n"
                  + "    \"stolen\",\n"
                  + "    \"unavailable\",\n"
                  + "    \"unknown\",\n"
                  + "    \"ex:aCustomDisposition\",\n"
                  + "  ]\n"
                  + "}";
}