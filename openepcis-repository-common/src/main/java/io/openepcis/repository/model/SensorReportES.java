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
import com.fasterxml.jackson.annotation.JsonProperty;
import io.openepcis.epc.translator.exception.ValidationException;
import io.openepcis.model.epcis.SensorReport;
import io.openepcis.model.epcis.constants.CBVUrnPrefix;
import io.openepcis.model.epcis.format.CBVFormat;
import io.openepcis.model.epcis.format.FormatPreference;
import io.openepcis.repository.util.CBVFormatUtil;
import io.openepcis.repository.util.EPCFormatUtil;
import io.openepcis.repository.util.EventConvertor;
import java.net.URI;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.collections4.MapUtils;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SensorReportES implements ESModel<SensorReport> {

  private CbvES type;
  private String exception;
  private EpcES deviceID;
  private EpcES deviceMetadata;
  private EpcES rawData;
  private URI dataProcessingMethod;
  private OffsetDateTime time;
  private URI microorganism;
  private URI chemicalSubstance;
  private Double value;
  private String component;
  private String stringValue;
  private Boolean booleanValue;
  private String hexBinaryValue;
  private URI uriValue;
  private Double minValue;
  private Double maxValue;
  private Double meanValue;

  @JsonProperty("sDev")
  private Double sDev;

  private Double percRank;
  private Double percValue;
  private String uom;
  private List<Map<String, Object>> userExtensions;
  private List<Map<String, Object>> innerUserExtensions;

  public SensorReportES(final SensorReport sensorReport, final List<Object> context)
      throws ValidationException {
    if (null != sensorReport.getType()) {

      this.setType(
          CBVFormatUtil.buildCbvESForStandardVocab(
              sensorReport.getType().toString(), CBVUrnPrefix.SENSOR_TYPE, context));
    }
    this.setException(sensorReport.getException());
    if (null != sensorReport.getDeviceID())
      this.setDeviceID(EPCFormatUtil.buildEpc(sensorReport.getDeviceID().toString(), context));
    if (null != sensorReport.getDeviceMetadata())
      this.setDeviceMetadata(
          EPCFormatUtil.buildEpc(sensorReport.getDeviceMetadata().toString(), context));
    if (null != sensorReport.getRawData())
      this.setRawData(EPCFormatUtil.buildEpc(sensorReport.getRawData().toString(), context));
    this.setDataProcessingMethod(sensorReport.getDataProcessingMethod());
    this.setTime(sensorReport.getTime());
    this.setMicroorganism(sensorReport.getMicroorganism());
    this.setChemicalSubstance(sensorReport.getChemicalSubstance());
    this.setValue(sensorReport.getValue());
    this.setComponent(sensorReport.getComponent());
    this.setStringValue(sensorReport.getStringValue());
    this.setBooleanValue(sensorReport.getBooleanValue());
    this.setHexBinaryValue(sensorReport.getHexBinaryValue());
    this.setUriValue(sensorReport.getUriValue());
    this.setMinValue(sensorReport.getMinValue());
    this.setMaxValue(sensorReport.getMaxValue());
    this.setMeanValue(sensorReport.getMeanValue());
    this.setSDev(sensorReport.getSDev());
    this.setPercRank(sensorReport.getPercRank());
    this.setPercValue(sensorReport.getPercValue());
    this.setUom(sensorReport.getUom());
    if (MapUtils.isNotEmpty(sensorReport.getUserExtensions())) {
      this.setUserExtensions(
          EventConvertor.getUserExtensionsFromCoreModel(context, sensorReport.getUserExtensions()));
    }
    if (MapUtils.isNotEmpty(sensorReport.getInnerUserExtensions())) {
      this.setInnerUserExtensions(
          EventConvertor.getUserExtensionsFromCoreModel(
              context, sensorReport.getInnerUserExtensions()));
    }
  }

  @Override
  @JsonIgnore
  public SensorReport getCoreModel() {
    return SensorReport.builder()
        .type(
            URI.create(
                CBVFormatUtil.getCbvInExpectedFormat(
                    this.getType(), CBVFormat.No_Preference.name())))
        .exception(this.exception)
        .deviceID(EPCFormatUtil.getEpcAsCaptured(this.getDeviceID()))
        .deviceMetadata(EPCFormatUtil.getEpcAsCaptured(this.getDeviceMetadata()))
        .rawData(EPCFormatUtil.getEpcAsCaptured(this.getRawData()))
        .dataProcessingMethod(this.getDataProcessingMethod())
        .time(this.getTime())
        .microorganism(this.getMicroorganism())
        .chemicalSubstance(this.getChemicalSubstance())
        .value(this.getValue())
        .component(this.getComponent())
        .stringValue(this.getStringValue())
        .booleanValue(this.getBooleanValue())
        .hexBinaryValue(this.getHexBinaryValue())
        .uriValue(this.getUriValue())
        .minValue(this.getMinValue())
        .maxValue(this.getMaxValue())
        .meanValue(this.getMeanValue())
        .sDev(this.getSDev())
        .percRank(this.getPercRank())
        .percValue(this.getPercValue())
        .uom(this.getUom())
        .userExtensions(EventConvertor.getCoreModelUserExtensions(this.getUserExtensions()))
        .innerUserExtensions(
            EventConvertor.getCoreModelUserExtensions(this.getInnerUserExtensions()))
        .build();
  }

  @Override
  public SensorReport getCoreModel(List<Object> context) {
    return SensorReport.builder()
        .type(
            URI.create(
                CBVFormatUtil.getCbvInExpectedFormat(
                    this.getType(), CBVFormat.No_Preference.name(), context)))
        .exception(this.exception)
        .deviceID(EPCFormatUtil.getEpcAsCaptured(this.getDeviceID()))
        .deviceMetadata(EPCFormatUtil.getEpcAsCaptured(this.getDeviceMetadata()))
        .rawData(EPCFormatUtil.getEpcAsCaptured(this.getRawData()))
        .dataProcessingMethod(this.getDataProcessingMethod())
        .time(this.getTime())
        .microorganism(this.getMicroorganism())
        .chemicalSubstance(this.getChemicalSubstance())
        .value(this.getValue())
        .component(this.getComponent())
        .stringValue(this.getStringValue())
        .booleanValue(this.getBooleanValue())
        .hexBinaryValue(this.getHexBinaryValue())
        .uriValue(this.getUriValue())
        .minValue(this.getMinValue())
        .maxValue(this.getMaxValue())
        .meanValue(this.getMeanValue())
        .sDev(this.getSDev())
        .percRank(this.getPercRank())
        .percValue(this.getPercValue())
        .uom(this.getUom())
        .userExtensions(EventConvertor.getCoreModelUserExtensions(this.getUserExtensions()))
        .innerUserExtensions(
            EventConvertor.getCoreModelUserExtensions(this.getInnerUserExtensions()))
        .build();
  }

  @Override
  public SensorReport getCoreModel(FormatPreference preference) {

    return SensorReport.builder()
        .type(
            this.type != null
                ? URI.create(
                    Objects.requireNonNull(
                        CBVFormatUtil.getCbvInExpectedFormat(
                            this.getType(), preference.getCbvFormat().name())))
                : null)
        .exception(this.exception)
        .deviceID(
            EPCFormatUtil.getEpcAsExpected(this.getDeviceID(), preference.getEpcFormat().name()))
        .deviceMetadata(
            EPCFormatUtil.getEpcAsExpected(
                this.getDeviceMetadata(), preference.getEpcFormat().name()))
        .rawData(
            EPCFormatUtil.getEpcAsExpected(this.getRawData(), preference.getEpcFormat().name()))
        .dataProcessingMethod(this.getDataProcessingMethod())
        .time(this.getTime())
        .microorganism(this.getMicroorganism())
        .chemicalSubstance(this.getChemicalSubstance())
        .value(this.getValue())
        .component(this.getComponent())
        .stringValue(this.getStringValue())
        .booleanValue(this.getBooleanValue())
        .hexBinaryValue(this.getHexBinaryValue())
        .uriValue(this.getUriValue())
        .minValue(this.getMinValue())
        .maxValue(this.getMaxValue())
        .meanValue(this.getMeanValue())
        .sDev(this.getSDev())
        .percRank(this.getPercRank())
        .percValue(this.getPercValue())
        .uom(this.getUom())
        .userExtensions(EventConvertor.getCoreModelUserExtensions(this.getUserExtensions()))
        .innerUserExtensions(
            EventConvertor.getCoreModelUserExtensions(this.getInnerUserExtensions()))
        .build();
  }

  @Override
  public SensorReport getCoreModel(FormatPreference preference, List<Object> context) {
    return SensorReport.builder()
        .type(
            this.type != null
                ? URI.create(
                    Objects.requireNonNull(
                        CBVFormatUtil.getCbvInExpectedFormat(
                            this.getType(), preference.getCbvFormat().name(), context)))
                : null)
        .exception(this.exception)
        .deviceID(
            EPCFormatUtil.getEpcAsExpected(
                this.getDeviceID(), preference.getEpcFormat().name(), context))
        .deviceMetadata(
            EPCFormatUtil.getEpcAsExpected(
                this.getDeviceMetadata(), preference.getEpcFormat().name(), context))
        .rawData(
            EPCFormatUtil.getEpcAsExpected(
                this.getRawData(), preference.getEpcFormat().name(), context))
        .dataProcessingMethod(this.getDataProcessingMethod())
        .time(this.getTime())
        .microorganism(this.getMicroorganism())
        .chemicalSubstance(this.getChemicalSubstance())
        .value(this.getValue())
        .component(this.getComponent())
        .stringValue(this.getStringValue())
        .booleanValue(this.getBooleanValue())
        .hexBinaryValue(this.getHexBinaryValue())
        .uriValue(this.getUriValue())
        .minValue(this.getMinValue())
        .maxValue(this.getMaxValue())
        .meanValue(this.getMeanValue())
        .sDev(this.getSDev())
        .percRank(this.getPercRank())
        .percValue(this.getPercValue())
        .uom(this.getUom())
        .userExtensions(EventConvertor.getCoreModelUserExtensions(this.getUserExtensions()))
        .innerUserExtensions(
            EventConvertor.getCoreModelUserExtensions(this.getInnerUserExtensions()))
        .build();
  }
}
