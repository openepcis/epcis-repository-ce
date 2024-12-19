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
import io.openepcis.model.epcis.Ilmd;
import io.openepcis.model.epcis.format.FormatPreference;
import io.openepcis.repository.util.EventConvertor;
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
public class IlmdES implements ESModel<Ilmd> {
  private List<Map<String, Object>> userExtensions;
  private List<Map<String, Object>> innerUserExtensions;

  public IlmdES(final Ilmd ilmd, List<Object> context) {
    this.setUserExtensions(
        MapUtils.isNotEmpty(ilmd.getUserExtensions())
            ? EventConvertor.getUserExtensionsFromCoreModel(context, ilmd.getUserExtensions())
            : null);
    this.setInnerUserExtensions(
        MapUtils.isNotEmpty(ilmd.getInnerUserExtensions())
            ? EventConvertor.getUserExtensionsFromCoreModel(context, ilmd.getInnerUserExtensions())
            : null);
  }

  @Override
  @JsonIgnore
  public Ilmd getCoreModel() {
    return Ilmd.builder()
        .userExtensions(EventConvertor.getCoreModelUserExtensions(this.getUserExtensions()))
        .innerUserExtensions((EventConvertor.getCoreModelUserExtensions(this.innerUserExtensions)))
        .build();
  }

  @Override
  public Ilmd getCoreModel(List<Object> context) {
    return getCoreModel();
  }

  @Override
  public Ilmd getCoreModel(FormatPreference preference) {
    return getCoreModel();
  }

  @Override
  public Ilmd getCoreModel(FormatPreference preference, List<Object> context) {
    return getCoreModel();
  }
}
