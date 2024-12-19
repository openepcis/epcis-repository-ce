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
import io.openepcis.model.epcis.PersistentDisposition;
import io.openepcis.model.epcis.constants.CBVUrnPrefix;
import io.openepcis.model.epcis.format.CBVFormat;
import io.openepcis.model.epcis.format.FormatPreference;
import io.openepcis.repository.util.CBVFormatUtil;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PersistentDispositionES implements ESModel<PersistentDisposition> {

  private List<CbvES> set;
  private List<CbvES> unset;

  public PersistentDispositionES(
      final PersistentDisposition persistentDisposition, List<Object> context) {
    this.setSet(
        CBVFormatUtil.buildCbvESListForStandardVocab(
            persistentDisposition.getSet(), CBVUrnPrefix.DISPOSITION, context));
    this.setUnset(
        CBVFormatUtil.buildCbvESListForStandardVocab(
            persistentDisposition.getUnset(), CBVUrnPrefix.DISPOSITION, context));
  }

  @Override
  @JsonIgnore
  public PersistentDisposition getCoreModel() {
    return PersistentDisposition.builder()
        .set(CBVFormatUtil.getCbvAsExpected(this.getSet(), CBVFormat.No_Preference.name()))
        .unset(CBVFormatUtil.getCbvAsExpected(this.getUnset(), CBVFormat.Never_Translates.name()))
        .build();
  }

  @Override
  public PersistentDisposition getCoreModel(List<Object> context) {
    return PersistentDisposition.builder()
        .set(CBVFormatUtil.getCbvAsExpected(this.getSet(), CBVFormat.No_Preference.name(), context))
        .unset(
            CBVFormatUtil.getCbvAsExpected(
                this.getUnset(), CBVFormat.Never_Translates.name(), context))
        .build();
  }

  @Override
  public PersistentDisposition getCoreModel(FormatPreference preference) {
    return PersistentDisposition.builder()
        .set(CBVFormatUtil.getCbvAsExpected(this.getSet(), preference.getCbvFormat().name()))
        .unset(CBVFormatUtil.getCbvAsExpected(this.getUnset(), preference.getCbvFormat().name()))
        .build();
  }

  @Override
  public PersistentDisposition getCoreModel(FormatPreference preference, List<Object> context) {
    return PersistentDisposition.builder()
        .set(
            CBVFormatUtil.getCbvAsExpected(
                this.getSet(), preference.getCbvFormat().name(), context))
        .unset(
            CBVFormatUtil.getCbvAsExpected(
                this.getUnset(), preference.getCbvFormat().name(), context))
        .build();
  }
}
