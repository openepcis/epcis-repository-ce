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
package io.openepcis.repository.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.openepcis.model.epcis.ErrorDeclaration;
import io.openepcis.model.epcis.constants.CBVUrnPrefix;
import io.openepcis.model.epcis.format.CBVFormat;
import io.openepcis.model.epcis.format.FormatPreference;
import io.openepcis.repository.util.CBVFormatUtil;
import io.openepcis.repository.util.EventConvertor;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.collections4.MapUtils;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ErrorDeclarationES implements ESModel<ErrorDeclaration> {

  private CbvES reason;
  private List<String> correctiveEventIDs;
  private OffsetDateTime declarationTime;
  private List<Map<String, Object>> userExtensions;
  private List<Map<String, Object>> innerUserExtensions;

  public ErrorDeclarationES(final ErrorDeclaration errorDeclaration, List<Object> context) {
    this.setReason(
        CBVFormatUtil.buildCbvESForStandardVocab(
            errorDeclaration.getReason(), CBVUrnPrefix.ERROR_REASON, context));
    this.setCorrectiveEventIDs(errorDeclaration.getCorrectiveEventIDs());
    this.setDeclarationTime(errorDeclaration.getDeclarationTime());
    this.setUserExtensions(
        MapUtils.isNotEmpty(errorDeclaration.getUserExtensions())
            ? EventConvertor.getUserExtensionsFromCoreModel(
                context, errorDeclaration.getUserExtensions())
            : null);
    this.setInnerUserExtensions(
        MapUtils.isNotEmpty(errorDeclaration.getInnerUserExtensions())
            ? EventConvertor.getUserExtensionsFromCoreModel(
                context, errorDeclaration.getInnerUserExtensions())
            : null);
  }

  @Override
  @JsonIgnore
  public ErrorDeclaration getCoreModel() {
    return ErrorDeclaration.builder()
        .reason(
            CBVFormatUtil.getCbvInExpectedFormat(this.getReason(), CBVFormat.No_Preference.name()))
        .correctiveEventIDs(this.getCorrectiveEventIDs())
        .declarationTime(this.getDeclarationTime())
        .userExtensions(EventConvertor.getCoreModelUserExtensions(this.getUserExtensions()))
        .innerUserExtensions((EventConvertor.getCoreModelUserExtensions(this.innerUserExtensions)))
        .build();
  }

  @Override
  public ErrorDeclaration getCoreModel(List<Object> context) {
    return ErrorDeclaration.builder()
        .reason(
            CBVFormatUtil.getCbvInExpectedFormat(
                this.getReason(), CBVFormat.No_Preference.name(), context))
        .correctiveEventIDs(this.getCorrectiveEventIDs())
        .declarationTime(this.getDeclarationTime())
        .userExtensions(EventConvertor.getCoreModelUserExtensions(this.getUserExtensions()))
        .innerUserExtensions((EventConvertor.getCoreModelUserExtensions(this.innerUserExtensions)))
        .build();
  }

  @Override
  public ErrorDeclaration getCoreModel(FormatPreference preference) {
    final String cbvFormat =
        preference.getCbvFormat() != null
            ? preference.getCbvFormat().name()
            : CBVFormat.No_Preference.name();
    return ErrorDeclaration.builder()
        .reason(CBVFormatUtil.getCbvInExpectedFormat(this.getReason(), cbvFormat))
        .correctiveEventIDs(this.getCorrectiveEventIDs())
        .declarationTime(this.getDeclarationTime())
        .userExtensions(EventConvertor.getCoreModelUserExtensions(this.getUserExtensions()))
        .innerUserExtensions((EventConvertor.getCoreModelUserExtensions(this.innerUserExtensions)))
        .build();
  }

  @Override
  public ErrorDeclaration getCoreModel(FormatPreference preference, List<Object> context) {
    final String cbvFormat =
        preference.getCbvFormat() != null
            ? preference.getCbvFormat().name()
            : CBVFormat.No_Preference.name();
    return ErrorDeclaration.builder()
        .reason(CBVFormatUtil.getCbvInExpectedFormat(this.getReason(), cbvFormat, context))
        .correctiveEventIDs(this.getCorrectiveEventIDs())
        .declarationTime(this.getDeclarationTime())
        .userExtensions(EventConvertor.getCoreModelUserExtensions(this.getUserExtensions()))
        .innerUserExtensions((EventConvertor.getCoreModelUserExtensions(this.innerUserExtensions)))
        .build();
  }
}
