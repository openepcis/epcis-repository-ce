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
import io.openepcis.model.epcis.TransformationEvent;
import io.openepcis.model.epcis.format.CBVFormat;
import io.openepcis.model.epcis.format.EPCFormat;
import io.openepcis.model.epcis.format.FormatPreference;
import io.openepcis.repository.util.CBVFormatUtil;
import io.openepcis.repository.util.EPCFormatUtil;
import io.openepcis.repository.util.EventConvertor;
import lombok.*;
import org.apache.commons.collections4.CollectionUtils;

import java.util.*;

import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;

@JsonInclude(JsonInclude.Include.NON_NULL)
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TransformationEventES extends EPCISEventES {

    private List<EpcES> inputEPCList;
    private List<EpcES> outputEPCList;
    private List<QuantityListES> inputQuantityList;
    private List<QuantityListES> outputQuantityList;
    private String transformationID;
    private IlmdES ilmd;
    private Map<String, Object> ilmdXml;


    public TransformationEventES(
            final TransformationEvent transformationEvent, Map<String, Object> metadata)
            throws ValidationException {
        this(transformationEvent, metadata, transformationEvent.getContextInfo());
    }

    public TransformationEventES(
            final TransformationEvent transformationEvent, Map<String, Object> metadata, List<Object> context)
            throws ValidationException {
        super(transformationEvent, metadata, context);
        if (transformationEvent.getPersistentDisposition() != null) {
            this.setPersistentDisposition(
                    new PersistentDispositionES(
                            transformationEvent.getPersistentDisposition(), context));
        }
        this.setInputEPCList(
                EPCFormatUtil.buildListOfEpcES(transformationEvent.getInputEPCList(), context));
        this.setOutputEPCList(
                EPCFormatUtil.buildListOfEpcES(transformationEvent.getOutputEPCList(), context));
        if (isNotEmpty(transformationEvent.getInputQuantityList())) {
            this.setInputQuantityList(
                    transformationEvent.getInputQuantityList().stream()
                            .map(item -> new QuantityListES(item, context))
                            .toList());
        }
        if (isNotEmpty(transformationEvent.getOutputQuantityList())) {
            this.setOutputQuantityList(
                    transformationEvent.getOutputQuantityList().stream()
                            .map(item -> new QuantityListES(item, context))
                            .toList());
        }
        this.setTransformationID(transformationEvent.getTransformationID());
        if (transformationEvent.getIlmd() != null) {
            this.setIlmd(new IlmdES(transformationEvent.getIlmd(), context));
        }

        if (transformationEvent.getIlmdXml() != null) {
            this.setIlmdXml(transformationEvent.getIlmdXml());
        }

    }

    @JsonIgnore
    @Override
    public TransformationEvent getCoreModel() {
        return TransformationEvent.transformationEventBuilder()
                .eventID(this.getEventID())
                .hash(this.getHash())
                .captureId(this.getCaptureID())
                .sequenceInEPCISDoc(this.getSequenceInEPCISDoc())
                .eventTimeZoneOffset(this.getEventTimeZoneOffset())
                .eventTime(this.getEventTime())
                .recordTime(this.getRecordTime())
                .bizStep(
                        CBVFormatUtil.getCbvInExpectedFormat(
                                this.getBizStep(), CBVFormat.Never_Translates.name()))
                .disposition(
                        CBVFormatUtil.getCbvInExpectedFormat(
                                this.getDisposition(), CBVFormat.No_Preference.name()))
                .readPoint(EventConvertor.getCoreModel(this.getReadPoint()))
                .bizLocation(EventConvertor.getCoreModel(this.getBizLocation()))
                .errorDeclaration(EventConvertor.getCoreModel(this.getErrorDeclarationES()))
                .extension(EventConvertor.getCoreModelUserExtensions(this.getExtension()))
                .userExtensions(EventConvertor.getCoreModelUserExtensions(this.getUserExtensions()))
                .contextInfo(this.getContextInfo())
                .persistentDisposition(EventConvertor.getCoreModel(this.getPersistentDisposition()))
                .destinationList(EventConvertor.getCoreModel(this.getDestinationList(), getContextInfo()))
                .sourceList(EventConvertor.getCoreModel(this.getSourceList(), getContextInfo()))
                .sensorElementList(
                        EventConvertor.getCoreModel(this.getSensorElementList(), getContextInfo()))
                .inputEPCList(EPCFormatUtil.getEpcAsCaptured(this.getInputEPCList()))
                .outputEPCList(EPCFormatUtil.getEpcAsCaptured(this.getOutputEPCList()))
                .inputQuantityList(
                        EventConvertor.getCoreModel(this.getInputQuantityList(), getContextInfo()))
                .outputQuantityList(
                        EventConvertor.getCoreModel(this.getOutputQuantityList(), getContextInfo()))
                .transformationID(this.getTransformationID())
                .ilmd(EventConvertor.getCoreModel(this.getIlmd()))
                .ilmdXml(this.getIlmdXml())
                .certificationInfo(this.getCertificationInfo())
                .build();
    }

    @JsonIgnore
    @Override
    public TransformationEvent getCoreModel(FormatPreference preference) {
        return this.getCoreModel(preference, getContextInfo());
    }

    @JsonIgnore
    @Override
    public TransformationEvent getCoreModel(FormatPreference preference, List<Object> context) {

        final String epcFormat =
                preference.getEpcFormat() != null
                        ? preference.getEpcFormat().name()
                        : EPCFormat.Always_GS1_Digital_Link.name();
        final String cbvFormat =
                preference.getCbvFormat() != null
                        ? preference.getCbvFormat().name()
                        : CBVFormat.No_Preference.name();

        return TransformationEvent.transformationEventBuilder()
                .eventID(this.getEventID())
                .hash(this.getHash())
                .captureId(this.getCaptureID())
                .sequenceInEPCISDoc(this.getSequenceInEPCISDoc())
                .eventTimeZoneOffset(this.getEventTimeZoneOffset())
                .eventTime(this.getEventTime())
                .recordTime(this.getRecordTime())
                .bizStep(CBVFormatUtil.getCbvInExpectedFormat(this.getBizStep(), cbvFormat))
                .disposition(CBVFormatUtil.getCbvInExpectedFormat(this.getDisposition(), cbvFormat))
                .readPoint(EventConvertor.getCoreModel(this.getReadPoint(), preference, context))
                .bizLocation(
                        EventConvertor.getCoreModel(this.getBizLocation(), preference, context))
                .errorDeclaration(
                        EventConvertor.getCoreModel(this.getErrorDeclarationES(), context))
                .extension(EventConvertor.getCoreModelUserExtensions(this.getExtension()))
                .userExtensions(EventConvertor.getCoreModelUserExtensions(this.getUserExtensions()))
                .contextInfo(this.getContextInfo())
                .persistentDisposition(
                        EventConvertor.getCoreModel(this.getPersistentDisposition(), context))
                .destinationList(
                        EventConvertor.getCoreModel(this.getDestinationList(), preference, context))
                .sourceList(EventConvertor.getCoreModel(this.getSourceList(), preference, context))
                .sensorElementList(
                        EventConvertor.getCoreModel(this.getSensorElementList(), preference, context))
                .inputEPCList(
                        EPCFormatUtil.getEpcAsExpected(this.getInputEPCList(), epcFormat, context))
                .outputEPCList(
                        EPCFormatUtil.getEpcAsExpected(this.getOutputEPCList(), epcFormat, context))
                .inputQuantityList(
                        EventConvertor.getCoreModel(this.getInputQuantityList(), preference, context))
                .outputQuantityList(
                        EventConvertor.getCoreModel(this.getOutputQuantityList(), preference, context))
                .transformationID(this.getTransformationID())
                .ilmd(EventConvertor.getCoreModel(this.getIlmd(), context))
                .ilmdXml(this.getIlmdXml())
                .certificationInfo(this.getCertificationInfo())
                .build();
    }

    @JsonIgnore
    public Set<EpcES> getEpcs() {
        final Set<EpcES> epcs = new HashSet<>();
        if (CollectionUtils.isNotEmpty(inputEPCList))
            epcs.addAll(inputEPCList);
        if (CollectionUtils.isNotEmpty(outputEPCList))
            epcs.addAll(outputEPCList);
        if (CollectionUtils.isNotEmpty(inputQuantityList))
            epcs.addAll(inputQuantityList.stream().map(QuantityListES::getEpcClass).toList());
        if (CollectionUtils.isNotEmpty(outputQuantityList))
            epcs.addAll(outputQuantityList.stream().map(QuantityListES::getEpcClass).toList());
        return epcs;
    }
}
