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

import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.openepcis.epc.translator.exception.ValidationException;
import io.openepcis.model.epcis.SensorElementList;
import io.openepcis.model.epcis.format.FormatPreference;
import io.openepcis.repository.util.EventConvertor;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.collections4.MapUtils;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SensorElementListES implements ESModel<SensorElementList> {

  private SensorMetadataES sensorMetadata;
  private List<SensorReportES> sensorReport;
  private List<Map<String, Object>> userExtensions;
  private List<Map<String, Object>> innerUserExtensions;

  public SensorElementListES(final SensorElementList sensorElementList, final List<Object> context)
      throws ValidationException {
    if (sensorElementList.getSensorMetadata() != null) {
      this.setSensorMetadata(new SensorMetadataES(sensorElementList.getSensorMetadata(), context));
    }
    if (isNotEmpty(sensorElementList.getSensorReport())) {
      this.setSensorReport(
          sensorElementList.getSensorReport().stream()
              .map(sr -> new SensorReportES(sr, context))
              .collect(Collectors.toList()));
    }
    if (MapUtils.isNotEmpty(sensorElementList.getUserExtensions())) {
      this.setUserExtensions(
          EventConvertor.getUserExtensionsFromCoreModel(
              context, sensorElementList.getUserExtensions()));
    }
    if (MapUtils.isNotEmpty(sensorElementList.getInnerUserExtensions())) {
      this.setInnerUserExtensions(
          EventConvertor.getUserExtensionsFromCoreModel(
              context, sensorElementList.getInnerUserExtensions()));
    }
  }

  @Override
  @JsonIgnore
  public SensorElementList getCoreModel() {
    return SensorElementList.builder()
        .sensorMetadata(EventConvertor.getCoreModel(this.getSensorMetadata()))
        .sensorReport(EventConvertor.getCoreModel(this.getSensorReport()))
        .userExtensions(EventConvertor.getCoreModelUserExtensions(this.getUserExtensions()))
        .innerUserExtensions(
            EventConvertor.getCoreModelUserExtensions(this.getInnerUserExtensions()))
        .build();
  }

  @Override
  public SensorElementList getCoreModel(List<Object> context) {
    return SensorElementList.builder()
        .sensorMetadata(EventConvertor.getCoreModel(this.getSensorMetadata(), context))
        .sensorReport(EventConvertor.getCoreModel(this.getSensorReport(), context))
        .userExtensions(EventConvertor.getCoreModelUserExtensions(this.getUserExtensions()))
        .innerUserExtensions(
            EventConvertor.getCoreModelUserExtensions(this.getInnerUserExtensions()))
        .build();
  }

  @Override
  public SensorElementList getCoreModel(FormatPreference preference) {

    return SensorElementList.builder()
        .sensorMetadata(EventConvertor.getCoreModel(this.getSensorMetadata()))
        .sensorReport(EventConvertor.getCoreModel(this.getSensorReport()))
        .userExtensions(EventConvertor.getCoreModelUserExtensions(this.getUserExtensions()))
        .innerUserExtensions(
            EventConvertor.getCoreModelUserExtensions(this.getInnerUserExtensions()))
        .build();
  }

  @Override
  public SensorElementList getCoreModel(FormatPreference preference, List<Object> context) {
    return SensorElementList.builder()
        .sensorMetadata(EventConvertor.getCoreModel(this.getSensorMetadata(), preference, context))
        .sensorReport(EventConvertor.getCoreModel(this.getSensorReport(), preference, context))
        .userExtensions(EventConvertor.getCoreModelUserExtensions(this.getUserExtensions()))
        .innerUserExtensions(
            EventConvertor.getCoreModelUserExtensions(this.getInnerUserExtensions()))
        .build();
  }
}
