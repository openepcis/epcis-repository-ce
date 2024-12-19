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

import com.fasterxml.jackson.annotation.JsonInclude;
import io.openepcis.model.epcis.NamedQuerySubscription;
import io.openepcis.model.epcis.Schedule;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.net.URI;
import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NamedQuerySubscriptionES {
  private UUID subscriptionID;
  private String wsSessionID;
  private String queryName;
  private URI callbackUrl;
  private URI dest;
  private String signatureToken;
  private String secret;
  private OffsetDateTime minRecordTime;
  private OffsetDateTime initialRecordTime;
  private Boolean reportIfEmpty;
  private Boolean stream;
  private Schedule schedule;
  private @Builder.Default boolean deleted = false;
  private OffsetDateTime createdAt;
  private String epcFormat;
  private String cbvFormat;
  private String status;
  private String errorDescription;
  private String subscribedBy;
  private MetadataES metadata;

  public NamedQuerySubscriptionES(NamedQuerySubscription namedQuerySubscription){
    this.subscriptionID = namedQuerySubscription.getSubscriptionID();
    this.wsSessionID = namedQuerySubscription.getWsSessionID();
    this.queryName = namedQuerySubscription.getQueryName();
    this.minRecordTime = namedQuerySubscription.getMinRecordTime();
    this.initialRecordTime = namedQuerySubscription.getInitialRecordTime();
    this.reportIfEmpty = namedQuerySubscription.getReportIfEmpty();
    this.stream = namedQuerySubscription.getStream();
    this.schedule = namedQuerySubscription.getSchedule();
    this.deleted = namedQuerySubscription.getDeleted();
    this.createdAt = namedQuerySubscription.getCreatedAt();
    this.epcFormat = namedQuerySubscription.getEpcFormat();
    this.cbvFormat = namedQuerySubscription.getCbvFormat();
    this.subscribedBy = namedQuerySubscription.getSubscribedBy();
    if(Objects.nonNull(namedQuerySubscription.getDest())){
      this.dest = namedQuerySubscription.getDest();
    }
    if(Objects.nonNull(namedQuerySubscription.getSignatureToken())){
      this.signatureToken = namedQuerySubscription.getSignatureToken();
    }
  }

  public static NamedQuerySubscription mapToNamedQuerySubscription(NamedQuerySubscriptionES es) {
    return NamedQuerySubscription.builder()
            .subscriptionID(es.getSubscriptionID())
            .wsSessionID(es.getWsSessionID())
            .queryName(es.getQueryName())
            .dest(es.getDest())
            .signatureToken(es.getSignatureToken())
            .minRecordTime(es.getMinRecordTime())
            .initialRecordTime(es.getInitialRecordTime())
            .reportIfEmpty(es.getReportIfEmpty())
            .stream(es.getStream())
            .schedule(es.getSchedule())
            .deleted(es.isDeleted())
            .createdAt(es.getCreatedAt())
            .epcFormat(es.getEpcFormat())
            .cbvFormat(es.getCbvFormat())
            .subscribedBy(es.getSubscribedBy())
            .defaultGroup(es.getMetadata().getCapturedBy().get(0).getDefaultGroup())
            .build();
  }
}
