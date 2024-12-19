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

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import io.openepcis.repository.Constants;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MetadataES {
  private List<CapturedByES> capturedBy;
  private Boolean visible;

  public static MetadataES fromMetadata(Map<String, Object> metadata, String captureID, OffsetDateTime recordTime) {
    return MetadataES.builder()
            .capturedBy(Collections.singletonList(CapturedByES.fromMetadata(metadata, captureID, recordTime)))
            .visible(getVisibility(metadata))
            .build();
  }

  private static boolean getVisibility(Map<String, Object> metadata) {
    return Optional.ofNullable(metadata.get(Constants.VISIBLE))
            .map(value -> (Boolean) value)
            .orElse(false);
  }

}
