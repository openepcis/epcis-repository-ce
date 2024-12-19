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
import io.openepcis.model.epcis.QuantityList;
import io.openepcis.model.epcis.format.EPCFormat;
import io.openepcis.model.epcis.format.FormatPreference;
import io.openepcis.repository.util.EPCFormatUtil;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class QuantityListES implements ESModel<QuantityList> {

  private EpcES epcClass;
  private Float quantity;
  private String uom;

  public QuantityListES(QuantityList quantityList, List<Object> context)
      throws ValidationException {
    this.setEpcClass(EPCFormatUtil.buildClassLevelEpc(quantityList.getEpcClass(), context));
    this.setQuantity(quantityList.getQuantity());
    this.setUom(quantityList.getUom());
  }

  @Override
  @JsonIgnore
  public QuantityList getCoreModel() {
    return QuantityList.builder()
        .epcClass(this.getEpcClass().getAsCaptured())
        .quantity(this.getQuantity())
        .uom(this.getUom())
        .build();
  }

  @Override
  public QuantityList getCoreModel(List<Object> context) {
    return getCoreModel();
  }

  @Override
  public QuantityList getCoreModel(FormatPreference preference) {
    final String epcFormat =
        preference.getEpcFormat() != null
            ? preference.getEpcFormat().name()
            : EPCFormat.Always_GS1_Digital_Link.name();

    return QuantityList.builder()
        .epcClass(EPCFormatUtil.getEpcInExpectedFormat(this.getEpcClass(), epcFormat))
        .quantity(this.getQuantity())
        .uom(this.getUom())
        .build();
  }

  @Override
  public QuantityList getCoreModel(FormatPreference preference, List<Object> context) {
    final String epcFormat =
        preference.getEpcFormat() != null
            ? preference.getEpcFormat().name()
            : EPCFormat.Always_GS1_Digital_Link.name();
    return QuantityList.builder()
        .epcClass(EPCFormatUtil.getEpcInExpectedFormat(this.getEpcClass(), epcFormat, context))
        .quantity(this.getQuantity())
        .uom(this.getUom())
        .build();
  }
}
