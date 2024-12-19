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
import io.openepcis.model.epcis.TransactionEvent;
import io.openepcis.model.epcis.extension.OpenEPCISExtension;
import io.openepcis.model.epcis.format.CBVFormat;
import io.openepcis.model.epcis.format.EPCFormat;
import io.openepcis.model.epcis.format.FormatPreference;
import io.openepcis.repository.util.CBVFormatUtil;
import io.openepcis.repository.util.EPCFormatUtil;
import io.openepcis.repository.util.EventConvertor;

import java.util.*;

import lombok.*;
import org.apache.commons.collections4.CollectionUtils;

@JsonInclude(JsonInclude.Include.NON_NULL)
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TransactionEventES extends EPCISEventES {

    private List<BizTransactionListES> bizTransactionList;
    private EpcES parentID;
    private List<EpcES> epcList;
    private List<QuantityListES> quantityList;

    public TransactionEventES(final TransactionEvent transactionEvent, Map<String, Object> metadata)
            throws ValidationException {
        this(transactionEvent, metadata, transactionEvent.getContextInfo());
    }

    public TransactionEventES(final TransactionEvent transactionEvent, Map<String, Object> metadata, List<Object> context)
            throws ValidationException {
        super(transactionEvent, metadata, context);
        this.setAction(transactionEvent.getAction());
        if (isNotEmpty(transactionEvent.getBizTransactionList())) {
            this.setBizTransactionList(
                    transactionEvent.getBizTransactionList().stream()
                            .map(item -> new BizTransactionListES(item, context))
                            .toList());
        }
        this.setParentID(EPCFormatUtil.buildEpc(transactionEvent.getParentID(), context));
        this.setEpcList(
                EPCFormatUtil.buildListOfEpcES(transactionEvent.getEpcList(), context));
        if (isNotEmpty(transactionEvent.getQuantityList())) {
            this.setQuantityList(
                    transactionEvent.getQuantityList().stream()
                            .map(item -> new QuantityListES(item, context))
                            .toList());
        }
    }

    @JsonIgnore
    @Override
    public TransactionEvent getCoreModel() {
        return TransactionEvent.transactionEventBuilder()
                .eventID(this.getEventID())
                .openEPCISExtension(OpenEPCISExtension.builder()
                        .hash(this.getHash())
                        .captureID(this.getCaptureID())
                        .sequenceInEPCISDoc(this.getSequenceInEPCISDoc())
                        .build())
                .eventTimeZoneOffset(this.getEventTimeZoneOffset())
                .eventTime(this.getEventTime())
                .recordTime(this.getRecordTime())
                .action(this.getAction())
                .bizStep(
                        CBVFormatUtil.getCbvInExpectedFormat(
                                this.getBizStep(), CBVFormat.No_Preference.name(), getContextInfo()))
                .disposition(
                        CBVFormatUtil.getCbvInExpectedFormat(
                                this.getDisposition(), CBVFormat.No_Preference.name(), getContextInfo()))
                .readPoint(EventConvertor.getCoreModel(this.getReadPoint(), getContextInfo()))
                .bizLocation(EventConvertor.getCoreModel(this.getBizLocation(), getContextInfo()))
                .errorDeclaration(
                        EventConvertor.getCoreModel(this.getErrorDeclarationES(), getContextInfo()))
                .extension(EventConvertor.getCoreModelUserExtensions(this.getExtension()))
                .userExtensions(EventConvertor.getCoreModelUserExtensions(this.getUserExtensions()))
                .contextInfo(this.getContextInfo())
                .destinationList(EventConvertor.getCoreModel(this.getDestinationList(), getContextInfo()))
                .sourceList(EventConvertor.getCoreModel(this.getSourceList(), getContextInfo()))
                .sensorElementList(
                        EventConvertor.getCoreModel(this.getSensorElementList(), getContextInfo()))
                .quantityList(EventConvertor.getCoreModel(this.getQuantityList(), getContextInfo()))
                .epcList(EPCFormatUtil.getEpcAsCaptured(this.getEpcList()))
                .bizTransactionList(
                        EventConvertor.getCoreModel(this.getBizTransactionList(), getContextInfo()))
                .parentID(
                        EPCFormatUtil.getEpcInExpectedFormat(
                                this.getParentID(), EPCFormat.Always_GS1_Digital_Link.name(), getContextInfo()))
                .certificationInfo(this.getCertificationInfo())
                .build();
    }

    @JsonIgnore
    @Override
    public TransactionEvent getCoreModel(FormatPreference preference) {
        return this.getCoreModel(preference, getContextInfo());
    }

    @JsonIgnore
    @Override
    public TransactionEvent getCoreModel(FormatPreference preference, List<Object> context) {

        final String epcFormat =
                preference.getEpcFormat() != null
                        ? preference.getEpcFormat().name()
                        : EPCFormat.Always_GS1_Digital_Link.name();
        final String cbvFormat =
                preference.getCbvFormat() != null
                        ? preference.getCbvFormat().name()
                        : CBVFormat.No_Preference.name();

        return TransactionEvent.transactionEventBuilder()
                .eventID(this.getEventID())
                .openEPCISExtension(OpenEPCISExtension.builder()
                        .hash(this.getHash())
                        .captureID(this.getCaptureID())
                        .sequenceInEPCISDoc(this.getSequenceInEPCISDoc())
                        .build())
                .eventTimeZoneOffset(this.getEventTimeZoneOffset())
                .eventTime(this.getEventTime())
                .recordTime(this.getRecordTime())
                .action(this.getAction())
                .bizStep(
                        CBVFormatUtil.getCbvInExpectedFormat(this.getBizStep(), cbvFormat, context))
                .disposition(
                        CBVFormatUtil.getCbvInExpectedFormat(
                                this.getDisposition(), cbvFormat, context))
                .readPoint(EventConvertor.getCoreModel(this.getReadPoint(), preference, context))
                .bizLocation(
                        EventConvertor.getCoreModel(this.getBizLocation(), preference, context))
                .errorDeclaration(
                        EventConvertor.getCoreModel(this.getErrorDeclarationES(), context))
                .extension(EventConvertor.getCoreModelUserExtensions(this.getExtension()))
                .userExtensions(EventConvertor.getCoreModelUserExtensions(this.getUserExtensions()))
                .contextInfo(this.getContextInfo())
                .destinationList(
                        EventConvertor.getCoreModel(this.getDestinationList(), preference, context))
                .sourceList(EventConvertor.getCoreModel(this.getSourceList(), preference, context))
                .sensorElementList(
                        EventConvertor.getCoreModel(this.getSensorElementList(), preference, context))
                .quantityList(
                        EventConvertor.getCoreModel(this.getQuantityList(), preference, context))
                .epcList(EPCFormatUtil.getEpcAsExpected(this.getEpcList(), epcFormat, context))
                .bizTransactionList(
                        EventConvertor.getCoreModel(this.getBizTransactionList(), context))
                .parentID(
                        EPCFormatUtil.getEpcInExpectedFormat(this.getParentID(), epcFormat, context))
                .certificationInfo(this.getCertificationInfo())
                .build();
    }

    @JsonIgnore
    public Set<EpcES> getEpcs() {
        final Set<EpcES> epcs = new HashSet<>();
        if (CollectionUtils.isNotEmpty(epcList))
            epcs.addAll(epcList);
        if (CollectionUtils.isNotEmpty(quantityList))
            epcs.addAll(quantityList.stream().map(QuantityListES::getEpcClass).toList());
        return epcs;
    }
}
