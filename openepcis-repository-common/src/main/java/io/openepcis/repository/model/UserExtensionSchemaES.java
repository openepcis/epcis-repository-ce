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
import io.openepcis.model.epcis.UserExtensionSchema;
import java.time.OffsetDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@JsonInclude(JsonInclude.Include.NON_NULL)
@EqualsAndHashCode
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserExtensionSchemaES {
  private String id;
  private String namespace;
  private String jsonSchemaS3Key;
  private String jsonSchemaUrl;
  private String defaultPrefix;
  private OffsetDateTime createdAt;
  private OffsetDateTime updatedAt;
  private String createdBy;
  private String updatedBy;
  private MetadataES metadata;

  public UserExtensionSchemaES(UserExtensionSchema userExtensionSchema, String defaultGroup) {
    this.id = userExtensionSchema.getId();
    this.namespace = userExtensionSchema.getNamespace();
    this.jsonSchemaS3Key = userExtensionSchema.getJsonSchemaS3Key();
    this.jsonSchemaUrl = userExtensionSchema.getJsonSchemaUrl();
    this.defaultPrefix = userExtensionSchema.getDefaultPrefix();
    this.createdAt = userExtensionSchema.getCreatedAt();
    this.createdBy = userExtensionSchema.getCreatedBy();
    this.updatedAt = userExtensionSchema.getUpdatedAt();
    this.updatedBy = userExtensionSchema.getUpdatedBy();
    this.setMetadata(MetadataES.builder()
            .capturedBy(List.of(CapturedByES.builder()
                    .timestamp(userExtensionSchema.getCreatedAt())
                    .userID(userExtensionSchema.getCreatedBy())
                    .defaultGroup(defaultGroup)
                    .build()))
            .build());
  }
}
