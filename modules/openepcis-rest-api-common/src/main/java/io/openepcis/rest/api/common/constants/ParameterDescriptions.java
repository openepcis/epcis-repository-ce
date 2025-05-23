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

public interface ParameterDescriptions {
  String PER_PAGE =
      "Parameter to control pagination. perPage specifies the maximum number of events returned in one batch.";
  String NEXT_PAGE_TOKEN =
      "Parameter to control pagination. nextPageToken specifies the link to events for next batch.";
  String EVENT_TYPE =
      "If specified, the result will only include events whose `type` matches one of the types specified in the parameter value. Each element of the parameter value may be one of the following strings: `ObjectEvent`, `AggregationEvent`, `TransactionEvent`, `TransformationEvent` or `AssociationEvent`. An element of the parameter value may also be the name of an extension event type. If omitted, all event types will be considered for inclusion in the result.";
  String GE_EVENT_TIME =
      "If specified, only events with `eventTime` greater than or equal to the specified value will be included in the result. If omitted, events are included regardless of their `eventTime` (unless constrained by the `LT_eventTime` parameter).";
  String LT_EVENT_TIME =
      "If specified, only events with `eventTime` less than the specified value will be included in the result. If omitted, events are included regardless of their `eventTime` (unless constrained by the `GE_eventTime` parameter).";
  String GE_RECORD_TIME =
      "If provided, only events with `recordTime` greater than or equal to the specified value will be returned. The automatic limitation based on event record time (section 8.2.5.2) may implicitly provide a constraint similar to this parameter. If omitted, events are included regardless of their `recordTime`, other than automatic limitation based on event record time";
  String LT_RECORD_TIME =
      "If provided, only events with `recordTime` less than the specified value will be returned. If omitted, events are included regardless of their `recordTime` (unless constrained by the `GE_recordTime` parameter or the automatic limitation based on event record time)";
  String EQ_ACTION =
      "If specified, the result will only include events that (a) have an `action` field; and where (b) the value of the `action` field matches one of the specified values. The properties of the value of this parameter each must be one of the strings `ADD`, `OBSERVE`, or `DELETE`; if not, the implementation SHALL raise a `QueryParameterException`. If omitted, events are included regardless of their `action` field.";
  String EQ_BIZ_STEP =
      "If specified, the result will only include events that (a) have a non-null `bizStep` field; and where (b) the value of the `bizStep` field matches one of the specified values. - see <a href=\"https://ref.gs1.org/cbv/BizStep\" target=\"_blank\">CBV BizStep</a> for standard values.  Standard values should be expressed as bare words, e.g. `shipping`, whereas custom values should be expressed as URIs or CURIEs for which the namespace prefix is defined. If this parameter is omitted, events are returned regardless of the value of the `bizStep` field or whether the `bizStep` field exists at all.";
  String EQ_DISPOSITION =
      "If specified, the result will only include events that (a) have a non-null `disposition` field; and where (b) the value of the `disposition` field matches one of the specified values. - see <a href=\"https://ref.gs1.org/cbv/Disp\" target=\"_blank\">CBV Disposition</a> for standard values.  Standard values should be expressed as bare words, e.g. `in_transit`, whereas custom values should be expressed as URIs or CURIEs for which the namespace prefix is defined. If this parameter is omitted, events are returned regardless of the value of the `disposition` field or whether the `disposition` field exists at all.";
  String EQ_PERSISTENT_DISPOSITION_SET =
      "If specified, the result will only include events that (a) have a non-null `persistentDisposition` field; and where (b) the value of the `set` field within the value of the `persistentDisposition` field matches one of the specified values. - see <a href=\"https://ref.gs1.org/cbv/Disp\" target=\"_blank\">CBV Disposition</a> for standard values.  Standard values should be expressed as bare words, e.g. `in_transit`, whereas custom values should be expressed as URIs or CURIEs for which the namespace prefix is defined. If this parameter is omitted, events are returned regardless of the value of the `set` field within `persistentDisposition` field or whether the `persistentDisposition` field exists at all.";
  String EQ_PERSISTENT_DISPOSITION_UNSET =
      "If specified, the result will only include events that (a) have a non-null `persistentDisposition` field; and where (b) the value of the `unset` field within the value of the `persistentDisposition` field matches one of the specified values. - see <a href=\"https://ref.gs1.org/cbv/Disp\" target=\"_blank\">CBV Disposition</a> for standard values.  Standard values should be expressed as bare words, e.g. `in_transit`, whereas custom values should be expressed as URIs or CURIEs for which the namespace prefix is defined. If this parameter is omitted, events are returned regardless of the value of the `unset` field within `persistentDisposition` field or whether the `persistentDisposition` field exists at all.";
  String EQ_READ_POINT =
      "If specified, the result will only include events that (a) have a non-null `readPoint` field; and where (b) the value of the `readPoint` field matches one of the specified URIs. If this parameter and `WD_readPoint` are both omitted, events are returned regardless of the value of the `readPoint` field or whether the `readPoint` field exists at all.";
  String WD_READ_POINT =
      "If specified, the result will only include events that (a) have a non-null `readPoint` field; and where (b) the value of the `readPoint` field matches one of the specified URIs, or is a direct or indirect descendant of one of the specified values. The meaning of 'direct or indirect descendant' is specified by master data, as described in section 6.5. (WD is an abbreviation for 'with descendants.') If this parameter and `EQ_readPoint` are both omitted, events are returned regardless of the value of the `readPoint` field or whether the `readPoint` field exists at all.";
  String EQ_BIZ_LOCATION =
      "If specified, the result will only include events that (a) have a non-null `bizLocation` field; and where (b) the value of the `bizLocation` field matches one of the specified URIs. If this parameter and `WD_bizLocation` are both omitted, events are returned regardless of the value of the `bizLocation` field or whether the `bizLocation` field exists at all.";
  String WD_BIZ_LOCATION =
      "If specified, the result will only include events that (a) have a non-null `bizLocation` field; and where (b) the value of the `bizLocation` field matches one of the specified URIs, or is a direct or indirect descendant of one of the specified values. The meaning of 'direct or indirect descendant' is specified by master data, as described in section 6.5. (WD is an abbreviation for 'with descendants.') If this parameter and `EQ_bizLocation` are both omitted, events are returned regardless of the value of the `bizLocation` field or whether the `bizLocation` field exists at all.";
  String EQ_TRANSFORMATION_ID =
      "If this parameter is specified, the result will only include events that (a) have a `transformationID` field (that is, `TransformationEvent`s or extension event type that extend `TransformationEvent`); and where (b) the `transformationID` field is equal to one of the values specified in this parameter.";
  String MATCH_EPC =
      "If this parameter is specified, the result will only include events that (a) have an `epcList` or a `childEPCs` field (that is, `ObjectEvent`, `AggregationEvent`, `TransactionEvent`, `AssociationEvent` or extension event types that extend one of those event types); and where (b) one of the EPCs listed in the `epcList` or `childEPCs` field (depending on event type) matches one of the URIs specified in this parameter, where the meaning of 'matches' is as specified in section 8.2.7.1.1.  If this parameter is omitted, events are included regardless of their `epcList` or `childEPCs` field or whether the `epcList` or `childEPCs` field exists.";
  String MATCH_PARENT_ID =
      "If this parameter is specified, the result will only include events that (a) have a `parentID` field (that is, `AggregationEvent`, `TransactionEvent`, `AssociationEvent` or extension event types that extend one of those event types); and where (b) one of the EPCs listed in the `parentID` field matches one of the URIs specified in this parameter, where the meaning of 'matches' is as specified in section 8.2.7.1.1.  If this parameter is omitted, events are included regardless of their `parentID` field or whether the `parentID` field exists.";
  String MATCH_INPUT_EPC =
      "If this parameter is specified, the result will only include events that (a) have an `inputEPCList` (that is, `TransformationEvent` or an extension event type that extends `TransformationEvent`); and where (b) one of the EPCs listed in the `inputEPCList` field matches one of the URIs specified in this parameter. The meaning of 'matches' is as specified in section 8.2.7.1.1. If this parameter is omitted, events are included regardless of their `inputEPCList` field or whether the `inputEPCList` field exists.";
  String MATCH_OUTPUT_EPC =
      "If this parameter is specified, the result will only include events that (a) have an `outputEPCList` (that is, `TransformationEvent` or an extension event type that extends `TransformationEvent`); and where (b) one of the EPCs listed in the `outputEPCList` field matches one of the URIs specified in this parameter. The meaning of 'matches' is as specified in section 8.2.7.1.1. If this parameter is omitted, events are included regardless of their `outputEPCList` field or whether the `outputEPCList` field exists.";
  String MATCH_ANY_EPC =
      "If this parameter is specified, the result will only include events that (a) have an `epcList` field, a `childEPCs` field, a `parentID` field, an `inputEPCList` field, or an `outputEPCList` field (that is, `ObjectEvent`, `AggregationEvent`, `TransactionEvent`, `TransformationEvent`, `AssociationEvent` or extension event types that extend one of those event types); and where (b) the `parentID` field or one of the EPCs listed in the `epcList`, `childEPCs`, `inputEPCList`, or `outputEPCList` field (depending on event type) matches one of URIs specified in this parameter. The meaning of 'matches' is as specified in section 8.2.7.1.1.";
  String MATCH_EPC_CLASS =
      "If this parameter is specified, the result will only include events that (a) have a `quantityList` or a `childQuantityList` field (that is, `ObjectEvent`, `AggregationEvent`, `TransactionEvent`, `AssociationEvent` or extension event types that extend one of those event types); and where (b) one of the EPC classes listed in the `quantityList` or `childQuantityList` field (depending on event type) matches one of the EPC patterns or URIs specified in this parameter. The result will also include QuantityEvents whose `epcClass` field matches one of the URIs specified in this parameter. The meaning of 'matches' is as specified in section 8.2.7.1.1.";
  String MATCH_INPUT_EPC_CLASS =
      "If this parameter is specified, the result will only include events that (a) have an `inputQuantityList` field (that is, `TransformationEvent` or extension event types that extend it); and where (b) one of the EPC classes listed in the `inputQuantityList` field (depending on event type) matches one of the EPC patterns or URIs specified in this parameter. The meaning of 'matches' is as specified in section 8.2.7.1.1";
  String MATCH_OUTPUT_EPC_CLASS =
      "If this parameter is specified, the result will only include events that (a) have an `outputQuantityList` field (that is, `TransformationEvent` or extension event types that extend it); and where (b) one of the EPC classes listed in the `outputQuantityList` field (depending on event type) matches one of the EPC patterns or URIs specified in this parameter. The meaning of 'matches' is as specified in section 8.2.7.1.1";
  String MATCH_ANY_EPC_CLASS =
      "If this parameter is specified, the result will only include events that (a) have a `quantityList`, `childQuantityList`, `inputQuantityList`, or `outputQuantityList` field (that is, `ObjectEvent`, `AggregationEvent`, `TransactionEvent`, `TransformationEvent`, `AssociationEvent` or extension event types that extend one of those event types); and where (b) one of the EPC classes listed in any of those fields matches one of the EPC patterns or URIs specified in this parameter. The result will also include `QuantityEvent`s whose `epcClass` field matches one of the URIs specified in this parameter. The meaning of 'matches' is as specified in section 8.2.7.1.1.";
  String EQ_QUANTITY =
      "(DEPCRECATED in EPCIS 1.1, REPURPOSED in EPCIS 2.0) If this parameter is specified, the result will only include events that (a) have a `quantity` field as part of a `QuantityElement`; and where (b) the `quantity` field is equal to the specified parameter.";
  String GT_QUANTITY =
      "(DEPCRECATED in EPCIS 1.1, REPURPOSED in EPCIS 2.0) If this parameter is specified, the result will only include events that (a) have a `quantity` field as part of a `QuantityElement`; and where (b) the `quantity` field is greater than the specified parameter.";
  String GE_QUANTITY =
      "(DEPCRECATED in EPCIS 1.1, REPURPOSED in EPCIS 2.0) If this parameter is specified, the result will only include events that (a) have a `quantity` field as part of a `QuantityElement`; and where (b) the `quantity` field is greater than or equal to the specified parameter.";
  String LT_QUANTITY =
      "(DEPCRECATED in EPCIS 1.1, REPURPOSED in EPCIS 2.0) If this parameter is specified, the result will only include events that (a) have a `quantity` field as part of a `QuantityElement`; and where (b) the `quantity` field is less than the specified parameter.";
  String LE_QUANTITY =
      "(DEPCRECATED in EPCIS 1.1, REPURPOSED in EPCIS 2.0) If this parameter is specified, the result will only include events that (a) have a `quantity` field as part of a `QuantityElement`; and where (b) the `quantity` field is less than or equal to the specified parameter.";
  String EQ_EVENT_ID =
      "If this parameter is specified, the result will only include events that (a) have a non-null `eventID` field; and where (b) the `eventID` field is equal to one of the values specified in this parameter. If this parameter is omitted, events are returned regardless of the value of the `eventID` field or whether the `eventID` field exists at all.";
  String EXISTS_ERROR_DECLARATION =
      "If this parameter is specified (and has a value of true), the result will only include events that contain an `ErrorDeclaration`. If this parameter is omitted (or has a value of false), events are returned regardless of whether they contain an `ErrorDeclaration`.";
  String GE_ERROR_DECLARATION_TIME =
      "If this parameter is specified, the result will only include events that (a) contain an `ErrorDeclaration`; and where (b) the value of the `errorDeclarationTime` field is greater than or equal to the specified value. If this parameter is omitted, events are returned regardless of whether they contain an `ErrorDeclaration` or what the value of the `errorDeclarationTime` field is.";
  String LT_ERROR_DECLARATION_TIME =
      "If this parameter is specified, the result will only include events that (a) contain an `ErrorDeclaration`; and where (b) the value of the `errorDeclarationTime` field is less than to the specified value. If this parameter is omitted, events are returned regardless of whether they contain an `ErrorDeclaration` or what the value of the `errorDeclarationTime` field is.";
  String EQ_ERROR_REASON =
      "If this parameter is specified, the result will only include events that (a) contain an `ErrorDeclaration`; and where (b) the error declaration contains a non-null `reason` field; and where (c) the `reason` field is equal to one of the values specified in this parameter. If this parameter is omitted, events are returned regardless of whether they contain an `ErrorDeclaration` or what the value of the `reason` field is.";
  String EQ_CORRECTIVE_EVENT_ID =
      "If this parameter is specified, the result will only include events that (a) contain an `ErrorDeclaration`; and where (b) one of the elements of the `correctiveEventIDs` list is equal to one of the values specified in this parameter. If this parameter is omitted, events are returned regardless of whether they contain an `ErrorDeclaration` or the contents of the `correctiveEventIDs` list.";
  String ORDER_BY =
      "If specified, names a single field that will be used to order the results. The `orderDirection` field specifies whether the ordering is in ascending sequence or descending sequence. Events included in the result that lack the specified field altogether may occur in any position within the result event list. The value of this parameter SHALL be one of: `eventTime`, `recordTime`, or the fully qualified name of an extension field whose type is Int, Float, Time, or String. A fully qualified fieldname is constructed as for the `EQ_fieldname` parameter. In the case of a field of type String, sorting SHALL be according to their case-sensitive lexical ordering, considering UTF-8/ASCII code values of each successive character. If omitted, no order is specified. The implementation MAY order the results in any order it chooses, and that order MAY differ even when the same query is executed twice on the same data. (In EPCIS 1.0, the value `quantity` was also permitted, but its use is deprecated in EPCIS 1.1.)";
  String ORDER_DIRECTION =
      "If specified and `orderBy` is also specified, specifies whether the results are ordered in ascending or descending sequence according to the key specified by `orderBy`. The value of this parameter must be one of `ASC` (for ascending order) or `DESC` (for descending order); if not, the implementation SHALL raise a `QueryParameterException`. If omitted, defaults to `DESC`.";
  String EVENT_COUNT_LIMIT =
      "If specified, the results will only include the first N events that match the other criteria, where N is the value of this parameter. The ordering specified by the `orderBy` and `orderDirection` parameters determine the meaning of “first” for this purpose. If omitted, all events matching the specified criteria will be included in the results. This parameter and `maxEventCount` are mutually exclusive; if both are specified, a `QueryParameterException` SHALL be raised. This parameter may only be used when `orderBy` is specified; if `orderBy` is omitted and `eventCountLimit` is specified, a `QueryParameterException` SHALL be raised. This parameter differs from `maxEventCount` in that this parameter limits the amount of data returned, whereas `maxEventCount` causes an exception to be thrown if the limit is exceeded. Explanation (non-normative): A context use of the `orderBy`, `orderDirection`, and `eventCountLimit` parameters is for extremal queries. For example, to select the most recent event matching some criteria, the query would include parameters that select events matching the desired criteria, and set `orderBy` to `eventTime`, `orderDirection` to `DESC`, and `eventCountLimit` to 1.";
  String MAX_EVENT_COUNT =
      "If specified, at most this many events will be included in the query result. If the query would otherwise return more than this number of events, a `QueryTooLargeException` SHALL be raised instead of a normal query result. This parameter and `eventCountLimit` are mutually exclusive; if both are specified, a `QueryParameterException` SHALL be raised. If this parameter is omitted, any number of events may be included in the query result. Note, however, that the EPCIS implementation is free to raise a `QueryTooLargeException` regardless of the setting of this parameter (see section 8.2.3).";
  String GE_START_TIME =
      "If specified, only events with `startTime` greater than or equal to the specified value will be included in the result. If omitted, events are included regardless of their `startTime` (unless constrained by the `LT_startTime` parameter).";
  String LT_START_TIME =
      "If specified, only events with `startTime` less than the specified value will be included in the result. If omitted, events are included regardless of their `startTime` (unless constrained by the `GE_startTime` parameter).";
  String GE_END_TIME =
      "If specified, only events with `endTime` greater than or equal to the specified value will be included in the result. If omitted, events are included regardless of their `endTime` (unless constrained by the `LT_endTime` parameter).";
  String LT_END_TIME =
      "If specified, only events with `endTime` less than the specified value will be included in the result. If omitted, events are included regardless of their `endTime` (unless constrained by the `GE_endTime` parameter).";
  String EQ_TYPE =
      "If this parameter is specified, the result will only include events that (a) accommodate one or more `sensorElement` fields; and where (b) the `type` attribute in one of these `sensorElement` fields is equal to one of the values specified in this parameter. If this parameter is omitted, events are returned regardless of the value of the `type` attribute or whether a `sensorElement` field exists at all. Standard values for `type` are defined at <a href=\"https://gs1.org/voc/MeasurementType\" target=\"_blank\">https://gs1.org/voc/MeasurementType</a>.  Standard values SHALL be expressed as bare words, e.g. `Temperature`.";
  String EQ_DEVICE_ID =
      "If this parameter is specified, the result will only include events that (a) accommodate a `deviceID` attribute; and where (b) the `deviceID` attribute is equal to one of the URIs specified in this parameter. If this parameter is omitted, events are returned regardless of the value of the `deviceID` attribute or whether the `deviceID` attribute exists at all.";
  String EQ_DATA_PROCESSING_METHOD =
      "If this parameter is specified, the result will only include events that (a) accommodate a `dataProcessingMethod` attribute; and where (b) the `dataProcessingMethod` attribute is equal to one of the URIs specified in this parameter. If this parameter is omitted, events are returned regardless of the value of the `dataProcessingMethod` attribute or whether the `dataProcessingMethod` attribute exists at all.";
  String EQ_MICROORGANISM =
      "If this parameter is specified, the result will only include events that (a) accommodate a `microorganism` attribute; and where (b) the `microorganism` attribute is equal to one of the URIs specified in this parameter. If this parameter is omitted, events are returned regardless of the value of the `microorganism` attribute or whether the `microorganism` attribute exists at all.";
  String EQ_CHEMICAL_SUBSTANCE =
      "If this parameter is specified, the result will only include events that (a) accommodate a `chemicalSubstance` attribute; and where (b) the `chemicalSubstance` attribute is equal to one of the URIs specified in this parameter. If this parameter is omitted, events are returned regardless of the value of the `chemicalSubstance` attribute or whether the `chemicalSubstance` attribute exists at all.";
  String EQ_BIZ_RULES =
      "If this parameter is specified, the result will only include events that (a) accommodate a `bizRules` attribute; and where (b) the `bizRules` attribute is equal to one of the URIs specified in this parameter. If this parameter is omitted, events are returned regardless of the value of the `bizRules` attribute or whether the `bizRules` attribute exists at all.";
  String EQ_STRING_VALUE =
      "If this parameter is specified, the result will only include events that (a) accommodate a `stringValue` attribute; and where (b) the `stringValue` attribute is equal to one of the strings specified in this parameter. If this parameter is omitted, events are returned regardless of the value of the `stringValue` attribute or whether the `stringValue` attribute exists at all.";
  String EQ_HEX_BINARY_VALUE =
      "If this parameter is specified, the result will only include events that (a) accommodate a `hexBinaryValue` attribute; and where (b) the `hexBinaryValue` attribute is equal to one of the strings specified in this parameter. If this parameter is omitted, events are returned regardless of the value of the `hexBinaryValue` attribute or whether the `hexBinaryValue` attribute exists at all.";
  String EQ_URI_VALUE =
      "If this parameter is specified, the result will only include events that (a) accommodate a `uriValue` attribute; and where (b) the `uriValue` attribute is equal to one of the strings specified in this parameter. If this parameter is omitted, events are returned regardless of the value of the `uriValue` attribute or whether the `uriValue` attribute exists at all.";
  String EQ_BOOLEAN_VALUE =
      "If this parameter is specified, the result will only include events that (a) accommodate a `booleanValue` attribute; and where (b) the `booleanValue` attribute is equal to the specified value (i.e. `true` or `false`). If this parameter is omitted, events are returned regardless of the value of the `booleanValue` attribute or whether the `booleanValue` attribute exists at all";
  String INITIAL_RECORD_TIME =
      "Parameter to specify from when onwards the query subscription applies. The default time is when the subscription was created.";
  String REPORT_IF_EMPTY = "Parameter to request notification even when the query result is empty";
  String SECOND = "Cron Trigger Parameter for second";
  String MINUTE = "Cron Trigger Parameter for minute";
  String HOUR = "Cron Trigger Parameter for hour";
  String DAY_OF_MONTH = "Cron Trigger Parameter for dayOfMonth";
  String MONTH = "Cron Trigger Parameter for Month";
  String DAY_OF_WEEK = "Cron Trigger Parameter for dayOfWeek";
  String GS1_EPCIS_VERSION = "The EPCIS version";
  String GS1_EPCIS_MIN = "The lowest EPCIS version supported.";
  String GS1_EPCIS_MAX = "The highest EPCIS version supported";
  String GS1_CBV_VERSION = "The Core Business Vocabulary version";
  String GS1_CBV_MIN = "The lowest Core Business Vocabulary version supported.";
  String GS1_CBV_MAX = "The highest Core Business Vocabulary version supported.";
  String GS1_EPC_FORMAT =
      "Header used by the client to indicate whether EPCs are expressed as GS1 Digital Link URIs or as EPC URNs.\n"
          + "It is also used by the server to announce which EPC formats are supported. \n"
          + "If absent the default value is `Always_GS1_Digital_Link`:\n"
          + "- No_Preference: No preference in the representation, i.e. any format is accepted.\n"
          + "- Always_GS1_Digital_Link: URIs are returned as GS1 Digital Link.\n"
          + "- Always_EPC_URN: URIs are returned as URN.\n"
          + "- Never_Translates: EPCs are never translated, i.e. the original format is kept.";
  String GS1_CBV_XML_FORMAT =
      "When requesting XML content-type only, users can use this header to request\n"
          + "receiving events with CBV values in either URN or Web URI format.\n"
          + "This option is not available for JSON/JSON-LD.\n"
          + "- No_Preference: The server chooses the representation.\n"
          + "- Always_Web_URI: CBV values are returned as Web URI.\n"
          + "- Always_URN: CBV values are returned as URNs.\n"
          + "- Never_Translates: The original format is kept.";
  String GS1_EXTENSIONS = "Specific EPCIS or CBV extensions supported (e.g., for FIT).";
  String GS1_CAPTURE_ERROR_BEHAVIOUR =
      "A header to control how the capture interface will behave in case of an error:\n"
          + "- `rollback`: \"All or nothing\". Either the capture job is entirely successful or all EPCIS events are rejected.\n"
          + "- `proceed`: \"Greedy capture\". The capture interface tries to capture as many EPCIS events as possible, even if there are errors.\n"
          + "The default behaviour is `rollback`, as in EPCIS 1.2.";
  String GS1_NEXT_PAGE_TOKEN_EXPIRES = "The expiry time for `nextPageToken`";
  String LINK =
      "A pagination header link. This header works together with the `perPage` and `nextPageToken` query string parameters.\n"
          + "As long as there are more resources to retrieve, the `Link` header contains the URL of the next page and\n"
          + "the attribute `rel=\"next\"`. The last page is indicated by the absence of the `rel=\"next\"`.";
  String LOCATION_QUERY =
      "Absolute or relative URL of the created query. The client can use the `Location` URL to obtain the named query definition.";
  String LOCATION_CAPTURE =
      "Absolute or relative URL of the capture job. The client can use the `Location` URL to obtain the state of the capture job.";
  String GS1_VENDOR_VERSION = "A versioning scheme that can be freely chosen by the vendor.";
  String GS1_EPCIS_CAPTURE_LIMIT =
      "The maximum number of EPCIS events that can be captured per call.";
  String GS1_EPCIS_CAPTURE_FILE_SIZE_LIMIT =
      "The maximum event document length in octets (8-bit bytes) for capture operations.";
  String ALLOW = "Lists the set of methods supported by the resource.";
  String ROLES_ALLOWED =
      "Sets the event-access level based on the roles provided. Only user with the roles provided in this field may later access the event being captured";
  String EVENT_ID =
          "The ID of an EPCIS event. An EPCIS event ID must be unique across all events in the system.";
  String EPC = "";

  String BIZ_STEP = "";

  String BIZ_LOCATION = "";

  String READ_POINT = "";

  String DISPOSITION = "";
  String CONTENT_TYPE = "";
}
