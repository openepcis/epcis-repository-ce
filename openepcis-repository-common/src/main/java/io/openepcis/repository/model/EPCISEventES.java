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
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import io.openepcis.epc.translator.exception.ValidationException;
import io.openepcis.model.epcis.Action;
import io.openepcis.model.epcis.EPCISEvent;
import io.openepcis.model.epcis.constants.CBVUrnPrefix;
import io.openepcis.model.epcis.extension.OpenEPCISExtension;
import io.openepcis.model.epcis.format.CBVFormat;
import io.openepcis.model.epcis.format.FormatPreference;
import io.openepcis.repository.util.CBVFormatUtil;
import io.openepcis.repository.util.EventConvertor;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.collections4.MapUtils;

import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
        @Type(value = ObjectEventES.class, name = "ObjectEvent"),
        @Type(value = TransformationEventES.class, name = "TransformationEvent"),
        @Type(value = AggregationEventES.class, name = "AggregationEvent"),
        @Type(value = AssociationEventES.class, name = "AssociationEvent"),
        @Type(value = TransactionEventES.class, name = "TransactionEvent")
})
@Data
@AllArgsConstructor
@NoArgsConstructor
public class EPCISEventES implements ESModel<EPCISEvent> {

    private String eventID;
    private String hash;
    private MetadataES metadata;
    private String eventTimeZoneOffset;
    private OffsetDateTime eventTime;
    private OffsetDateTime recordTime;
    private Action action;
    private CbvES bizStep;
    private CbvES disposition;
    private PersistentDispositionES persistentDisposition;
    private ReadPointES readPoint;
    private BizLocationES bizLocation;
    private ErrorDeclarationES errorDeclarationES;
    private Object certificationInfo;
    private List<Map<String, Object>> extension;
    private List<Map<String, Object>> userExtensions;
    private List<Map<String, Object>> innerUserExtensions;
    private List<Object> contextInfo;
    private List<SourceListES> sourceList;
    private List<DestinationListES> destinationList;
    private List<SensorElementListES> sensorElementList;
    private String captureID;
    private Integer sequenceInEPCISDoc;

    public EPCISEventES(final EPCISEvent epcisEvent, Map<String, Object> metadata)
            throws ValidationException {
        this(epcisEvent, metadata, epcisEvent.getContextInfo());
    }


    public EPCISEventES(final EPCISEvent epcisEvent, Map<String, Object> metadata, List<Object> context)
            throws ValidationException {
        setCaptureID(epcisEvent.getOpenEPCISExtension().getCaptureID());
        setSequenceInEPCISDoc(epcisEvent.getOpenEPCISExtension().getSequenceInEPCISDoc());
        setHash(epcisEvent.getOpenEPCISExtension().getHash());

        this.setEventTimeZoneOffset(epcisEvent.getEventTimeZoneOffset());
        this.setEventTime(epcisEvent.getEventTime());
        this.setRecordTime(epcisEvent.getRecordTime());

        if(MapUtils.isNotEmpty(metadata)) {
            this.setMetadata(MetadataES.fromMetadata(metadata, captureID, epcisEvent.getRecordTime()));
        }

        this.setAction(getAction());
        this.setBizStep(
                CBVFormatUtil.buildCbvESForStandardVocab(
                        epcisEvent.getBizStep(), CBVUrnPrefix.BIZSTEP, context));
        this.setDisposition(
                CBVFormatUtil.buildCbvESForStandardVocab(
                        epcisEvent.getDisposition(), CBVUrnPrefix.DISPOSITION, context));
        if (epcisEvent.getReadPoint() != null)
            this.setReadPoint(new ReadPointES(epcisEvent.getReadPoint(), context));
        if (epcisEvent.getBizLocation() != null)
            this.setBizLocation(
                    new BizLocationES(epcisEvent.getBizLocation(), context));
        if (epcisEvent.getErrorDeclaration() != null) {
            this.setErrorDeclarationES(
                    new ErrorDeclarationES(epcisEvent.getErrorDeclaration(), context));
        }
        if (MapUtils.isNotEmpty(epcisEvent.getExtension()))
            this.setExtension(
                    EventConvertor.getUserExtensionsFromCoreModel(
                            context, epcisEvent.getExtension()));
        if (MapUtils.isNotEmpty(epcisEvent.getUserExtensions()))
            this.setUserExtensions(
                    EventConvertor.getUserExtensionsFromCoreModel(
                            context, epcisEvent.getUserExtensions()));
        if (MapUtils.isNotEmpty(epcisEvent.getInnerUserExtensions()))
            this.setInnerUserExtensions(
                    EventConvertor.getUserExtensionsFromCoreModel(
                            context, epcisEvent.getInnerUserExtensions()));
        this.setContextInfo(epcisEvent.getContextInfo() != null && !epcisEvent.getContextInfo().isEmpty() ? epcisEvent.getContextInfo() : null);
        if (isNotEmpty(epcisEvent.getSourceList())) {
            this.setSourceList(
                    epcisEvent.getSourceList().stream()
                            .map(item -> new SourceListES(item, context))
                            .toList());
        }
        if (isNotEmpty(epcisEvent.getDestinationList())) {
            this.setDestinationList(
                    epcisEvent.getDestinationList().stream()
                            .map(item -> new DestinationListES(item, context))
                            .toList());
        }
        if (isNotEmpty(epcisEvent.getSensorElementList())) {
            this.setSensorElementList(
                    epcisEvent.getSensorElementList().stream()
                            .map(
                                    sensorElementList ->
                                            new SensorElementListES(sensorElementList, context))
                            .toList());
        }
        this.setEventID(epcisEvent.getEventID());
        this.setCertificationInfo(epcisEvent.getCertificationInfo());
    }

    @Override
    @JsonIgnore
    public EPCISEvent getCoreModel() {
        return EPCISEvent.builder()
                .eventID(this.getEventID())
                .openEPCISExtension(OpenEPCISExtension.builder()
                        .hash(this.getHash())
                        .captureID(this.getCaptureID())
                        .sequenceInEPCISDoc(this.getSequenceInEPCISDoc())
                        .build())
                .eventTimeZoneOffset(this.getEventTimeZoneOffset())
                .eventTime(this.getEventTime())
                .recordTime(this.getRecordTime())
                .bizStep(
                        CBVFormatUtil.getCbvInExpectedFormat(
                                this.getBizStep(), CBVFormat.Never_Translates.name(), getContextInfo()))
                .disposition(
                        CBVFormatUtil.getCbvInExpectedFormat(
                                this.getDisposition(), CBVFormat.Never_Translates.name(), getContextInfo()))
                .readPoint(EventConvertor.getCoreModel(this.getReadPoint(), getContextInfo()))
                .bizLocation(EventConvertor.getCoreModel(this.getBizLocation(), getContextInfo()))
                .errorDeclaration(
                        EventConvertor.getCoreModel(this.getErrorDeclarationES(), getContextInfo()))
                .extension(EventConvertor.getCoreModelUserExtensions(this.getExtension()))
                .userExtensions(EventConvertor.getCoreModelUserExtensions(this.getUserExtensions()))
                .innerUserExtensions((EventConvertor.getCoreModelUserExtensions(this.innerUserExtensions)))
                .contextInfo(this.getContextInfo() != null && !this.contextInfo.isEmpty() ? this.contextInfo : null)
                .destinationList(EventConvertor.getCoreModel(this.getDestinationList(), getContextInfo()))
                .sourceList(EventConvertor.getCoreModel(this.getSourceList(), getContextInfo()))
                .sensorElementList(
                        EventConvertor.getCoreModel(this.getSensorElementList(), getContextInfo()))
                .certificationInfo(this.getCertificationInfo())
                .build();
    }

    @Override
    public EPCISEvent getCoreModel(List<Object> context) {
        return getCoreModel();
    }

    @Override
    public EPCISEvent getCoreModel(FormatPreference preference) {
        return this.getCoreModel(preference, getContextInfo());
    }

    @Override
    public EPCISEvent getCoreModel(FormatPreference preference, List<Object> context) {
        return EPCISEvent.builder()
                .eventID(this.getEventID())
                .openEPCISExtension(OpenEPCISExtension.builder()
                        .hash(this.getHash())
                        .captureID(this.getCaptureID())
                        .sequenceInEPCISDoc(this.getSequenceInEPCISDoc())
                        .build())
                .eventTimeZoneOffset(this.getEventTimeZoneOffset())
                .eventTime(this.getEventTime())
                .recordTime(this.getRecordTime())
                .bizStep(
                        CBVFormatUtil.getCbvInExpectedFormat(
                                this.getBizStep(), CBVFormat.No_Preference.name(), context))
                .disposition(
                        CBVFormatUtil.getCbvInExpectedFormat(
                                this.getDisposition(), CBVFormat.No_Preference.name(), context))
                .readPoint(EventConvertor.getCoreModel(this.getReadPoint(), preference, context))
                .bizLocation(
                        EventConvertor.getCoreModel(this.getBizLocation(), preference, context))
                .errorDeclaration(
                        EventConvertor.getCoreModel(this.getErrorDeclarationES(), context))
                .extension(EventConvertor.getCoreModelUserExtensions(this.getExtension()))
                .userExtensions(EventConvertor.getCoreModelUserExtensions(this.getUserExtensions()))
                .innerUserExtensions((EventConvertor.getCoreModelUserExtensions(this.innerUserExtensions)))
                .contextInfo(this.getContextInfo())
                .destinationList(
                        EventConvertor.getCoreModel(this.getDestinationList(), preference, context))
                .sourceList(EventConvertor.getCoreModel(this.getSourceList(), preference, context))
                .sensorElementList(
                        EventConvertor.getCoreModel(this.getSensorElementList(), preference, context))
                .certificationInfo(this.getCertificationInfo())
                .build();
    }

    @JsonIgnore
    public Set<EpcES> getEpcs(EPCISEventES event) {
        final Set<EpcES> epcs = new HashSet<>();
        if (event instanceof ObjectEventES objectEventES) {
            return  objectEventES.getEpcs();
        } else if (event instanceof TransactionEventES transactionEventES) {
            return transactionEventES.getEpcs();
        } else if (event instanceof AssociationEventES associationEventES) {
            return associationEventES.getEpcs();
        } else if (event instanceof AggregationEventES aggregationEventES) {
            return aggregationEventES.getEpcs();
        } else if (event instanceof TransformationEventES transformationEventES) {
            return transformationEventES.getEpcs();
        }
        return epcs;
    }

}
