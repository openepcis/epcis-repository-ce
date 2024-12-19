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
import io.openepcis.epc.translator.exception.ValidationException;
import io.openepcis.model.epcis.BizLocation;
import io.openepcis.model.epcis.format.FormatPreference;
import io.openepcis.repository.util.EPCFormatUtil;
import io.openepcis.repository.util.EventConvertor;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.collections4.MapUtils;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BizLocationES implements ESModel<BizLocation> {
  private EpcES id;
  private List<Map<String, Object>> userExtensions;
  private List<Map<String, Object>> innerUserExtensions;

  public BizLocationES(final BizLocation bizLocation, List<Object> context)
      throws ValidationException {
    this.setId(EPCFormatUtil.buildEpc(bizLocation.getId().toString(), context));
    this.setUserExtensions(
        MapUtils.isNotEmpty(bizLocation.getUserExtensions())
            ? EventConvertor.getUserExtensionsFromCoreModel(
                context, bizLocation.getUserExtensions())
            : null);
    this.setInnerUserExtensions(
        MapUtils.isNotEmpty(bizLocation.getInnerUserExtensions())
            ? EventConvertor.getUserExtensionsFromCoreModel(
                context, bizLocation.getInnerUserExtensions())
            : null);
  }

  @Override
  @JsonIgnore
  public BizLocation getCoreModel() {
    return BizLocation.builder()
        .id(EPCFormatUtil.getEpcAsCaptured(this.getId()))
        .userExtensions(EventConvertor.getCoreModelUserExtensions(this.getUserExtensions()))
        .innerUserExtensions((EventConvertor.getCoreModelUserExtensions(this.innerUserExtensions)))
        .build();
  }

  @Override
  public BizLocation getCoreModel(List<Object> context) {
    return BizLocation.builder()
        .id(EPCFormatUtil.getEpcAsCaptured(this.getId()))
        .userExtensions(EventConvertor.getCoreModelUserExtensions(this.getUserExtensions()))
        .innerUserExtensions((EventConvertor.getCoreModelUserExtensions(this.innerUserExtensions)))
        .build();
  }

  @Override
  public BizLocation getCoreModel(FormatPreference preference) {
    return BizLocation.builder()
        .id(EPCFormatUtil.getEpcAsExpected(this.getId(), preference.getEpcFormat().name()))
        .userExtensions(EventConvertor.getCoreModelUserExtensions(this.getUserExtensions()))
        .innerUserExtensions((EventConvertor.getCoreModelUserExtensions(this.innerUserExtensions)))
        .build();
  }

  @Override
  public BizLocation getCoreModel(FormatPreference preference, List<Object> context) {
    return BizLocation.builder()
        .id(EPCFormatUtil.getEpcAsExpected(this.getId(), preference.getEpcFormat().name(), context))
        .userExtensions(EventConvertor.getCoreModelUserExtensions(this.getUserExtensions()))
        .innerUserExtensions((EventConvertor.getCoreModelUserExtensions(this.innerUserExtensions)))
        .build();
  }
}
