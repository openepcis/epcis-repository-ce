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

import io.openepcis.model.epcis.constants.CommonConstants;

public interface HeaderConstants {
  String GS1_EXTENSIONS = "GS1-Extensions";
  String ROLES_ALLOWED = "Roles-Allowed";

  String CONTENT_TYPE = "Content-Type";
  String GS1_CBV_VERSION = CommonConstants.GS1_CBV_VERSION;
  String GS1_EPCIS_VERSION = CommonConstants.GS1_EPCIS_VERSION;
  String GS1_EPCIS_MAX = CommonConstants.GS1_EPCIS_MAX;
  String GS1_EPCIS_MIN = CommonConstants.GS1_EPCIS_MIN;
  String GS1_CAPTURE_ERROR_BEHAVIOUR = CommonConstants.GS1_Capture_Error_Behaviour;
  String GS1_EPCIS_CAPTURE_LIMIT = CommonConstants.GS1_EPCIS_Capture_Limit;
  String GS1_CAPTURE_FILE_SIZE_LIMIT = CommonConstants.GS1_EPCIS_Capture_File_Size_Limit;
  String GS1_VENDOR_VERSION = CommonConstants.GS1_Vendor_VERSION;
  String GS1_CBV_XML_FORMAT = "GS1-CBV-XML-Format";
  String GS1_CBV_MAX = CommonConstants.GS1_CBV_MAX;
  String GS1_CBV_MIN = CommonConstants.GS1_CBV_MIN;
  String ALLOW = "Allow";
  String GS1_EPC_FORMAT = CommonConstants.GS1_EPC_FORMAT;
}
