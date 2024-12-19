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
import io.openepcis.epc.translator.exception.ValidationException;
import io.openepcis.model.epcis.SensorMetadata;
import io.openepcis.model.epcis.format.FormatPreference;
import io.openepcis.repository.util.EPCFormatUtil;
import io.openepcis.repository.util.EventConvertor;
import java.net.URI;
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
public class SensorMetadataES implements ESModel<SensorMetadata> {
  private OffsetDateTime time;
  private EpcES deviceID;
  private EpcES deviceMetaData;
  private EpcES rawData;
  private OffsetDateTime startTime;
  private OffsetDateTime endTime;
  private URI dataProcessingMethod;
  private URI bizRules;
  private List<Map<String, Object>> userExtensions;
  private List<Map<String, Object>> innerUserExtensions;

  public SensorMetadataES(final SensorMetadata sensorMetaData, List<Object> context)
      throws ValidationException {
    this.setTime(sensorMetaData.getTime());
    if (null != sensorMetaData.getDeviceID())
      this.setDeviceID(EPCFormatUtil.buildEpc(sensorMetaData.getDeviceID().toString(), context));
    if (null != sensorMetaData.getDeviceMetadata())
      this.setDeviceMetaData(
          EPCFormatUtil.buildEpc(sensorMetaData.getDeviceMetadata().toString(), context));
    if (null != sensorMetaData.getRawData())
      this.setRawData(EPCFormatUtil.buildEpc(sensorMetaData.getRawData().toString(), context));
    this.setStartTime(sensorMetaData.getStartTime());
    this.setEndTime(sensorMetaData.getEndTime());
    this.setDataProcessingMethod(sensorMetaData.getDataProcessingMethod());
    this.setBizRules(sensorMetaData.getBizRules());
    if (MapUtils.isNotEmpty(sensorMetaData.getUserExtensions())) {
      this.setUserExtensions(
          EventConvertor.getUserExtensionsFromCoreModel(
              context, sensorMetaData.getUserExtensions()));
    }
    if (MapUtils.isNotEmpty(sensorMetaData.getInnerUserExtensions())) {
      this.setInnerUserExtensions(
          EventConvertor.getUserExtensionsFromCoreModel(
              context, sensorMetaData.getInnerUserExtensions()));
    }
  }

  @Override
  @JsonIgnore
  public SensorMetadata getCoreModel() {
    return SensorMetadata.builder()
        .time(this.getTime())
        .deviceID(EPCFormatUtil.getEpcAsCaptured(this.getDeviceID()))
        .deviceMetadata(EPCFormatUtil.getEpcAsCaptured(this.getDeviceMetaData()))
        .rawData(EPCFormatUtil.getEpcAsCaptured(this.getRawData()))
        .startTime(this.getStartTime())
        .endTime(this.getEndTime())
        .dataProcessingMethod(this.getDataProcessingMethod())
        .bizRules(this.getBizRules())
        .userExtensions(EventConvertor.getCoreModelUserExtensions(this.getUserExtensions()))
        .innerUserExtensions(
            EventConvertor.getCoreModelUserExtensions(this.getInnerUserExtensions()))
        .build();
  }

  @Override
  public SensorMetadata getCoreModel(List<Object> context) {
    return getCoreModel();
  }

  @Override
  public SensorMetadata getCoreModel(FormatPreference preference) {

    return SensorMetadata.builder()
        .time(this.getTime())
        .deviceID(
            EPCFormatUtil.getEpcAsExpected(this.getDeviceID(), preference.getEpcFormat().name()))
        .deviceMetadata(
            EPCFormatUtil.getEpcAsExpected(
                this.getDeviceMetaData(), preference.getEpcFormat().name()))
        .rawData(
            EPCFormatUtil.getEpcAsExpected(this.getRawData(), preference.getEpcFormat().name()))
        .startTime(this.getStartTime())
        .endTime(this.getEndTime())
        .dataProcessingMethod(this.getDataProcessingMethod())
        .bizRules(this.getBizRules())
        .userExtensions(EventConvertor.getCoreModelUserExtensions(this.getUserExtensions()))
        .innerUserExtensions(
            EventConvertor.getCoreModelUserExtensions(this.getInnerUserExtensions()))
        .build();
  }

  @Override
  public SensorMetadata getCoreModel(FormatPreference preference, List<Object> context) {
    return SensorMetadata.builder()
        .time(this.getTime())
        .deviceID(
            EPCFormatUtil.getEpcAsExpected(
                this.getDeviceID(), preference.getEpcFormat().name(), context))
        .deviceMetadata(
            EPCFormatUtil.getEpcAsExpected(
                this.getDeviceMetaData(), preference.getEpcFormat().name(), context))
        .rawData(
            EPCFormatUtil.getEpcAsExpected(
                this.getRawData(), preference.getEpcFormat().name(), context))
        .startTime(this.getStartTime())
        .endTime(this.getEndTime())
        .dataProcessingMethod(this.getDataProcessingMethod())
        .bizRules(this.getBizRules())
        .userExtensions(EventConvertor.getCoreModelUserExtensions(this.getUserExtensions()))
        .innerUserExtensions(
            EventConvertor.getCoreModelUserExtensions(this.getInnerUserExtensions()))
        .build();
  }
}
