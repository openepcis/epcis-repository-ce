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
import io.openepcis.model.epcis.NamedQueryMetaData;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.Objects;

@JsonInclude(JsonInclude.Include.NON_NULL)
@AllArgsConstructor
@NoArgsConstructor
public class NamedQueryMetaDataES {
  @JsonProperty("name")
  private String name;

  @JsonProperty("createdAt")
  private OffsetDateTime createdAt;

  @JsonProperty("query")
  private Map<String, Object> epcisQuery;

  private boolean deleted;

  private MetadataES metadata;

  public NamedQueryMetaDataES(NamedQueryMetaData metaData) {
    this.name = metaData.getName();
    this.createdAt = metaData.getCreatedAt();
    this.epcisQuery = metaData.getEpcisQuery();
  }

  @JsonIgnore
  public NamedQueryMetaData getCoreModel() {
    return NamedQueryMetaData.builder()
        .name(this.name)
        .createdAt(this.createdAt)
        .epcisQuery(this.epcisQuery)
        .build();
  }

  public NamedQueryMetaDataES name(String name) {
    this.name = name;
    return this;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public NamedQueryMetaDataES createdAt(OffsetDateTime createdAt) {
    this.createdAt = createdAt;
    return this;
  }

  /**
   * Get createdAt
   *
   * @return createdAt
   */
  public OffsetDateTime getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(OffsetDateTime createdAt) {
    this.createdAt = createdAt != null ? createdAt : OffsetDateTime.now();
  }

  public NamedQueryMetaDataES epcisQuery(Map<String, Object> epcisQuery) {
    this.epcisQuery = epcisQuery;
    return this;
  }

  /**
   * Get epcisQuery
   *
   * @return epcisQuery
   */
  public Map<String, Object> getEpcisQuery() {
    return epcisQuery;
  }

  public void setEpcisQuery(Map<String, Object> epcisQuery) {
    this.epcisQuery = epcisQuery;
  }

  public boolean isDeleted() {
    return deleted;
  }

  public void setDeleted(boolean deleted) {
    this.deleted = deleted;
  }

  public MetadataES getMetadata() {
    return metadata;
  }

  public void setMetadata(MetadataES metadata) {
    this.metadata = metadata;
  }
  /**
   * Get activeSubscriptions
   *
   * @return activeSubscriptions
   */
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    NamedQueryMetaDataES namedQueryMetaDataES = (NamedQueryMetaDataES) o;
    return Objects.equals(this.name, namedQueryMetaDataES.name)
        && Objects.equals(this.createdAt, namedQueryMetaDataES.createdAt)
        && Objects.equals(this.epcisQuery, namedQueryMetaDataES.epcisQuery);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, createdAt, epcisQuery);
  }

  @Override
  public String toString() {
    return "class NamedQueryMetaDataES {\n"
        + "    name: "
        + toIndentedString(name)
        + "\n"
        + "    createdAt: "
        + toIndentedString(createdAt)
        + "\n"
        + "    epcisQuery: "
        + toIndentedString(epcisQuery)
        + "\n"
        + "}";
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces (except the first line).
   */
  private String toIndentedString(Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }
}
