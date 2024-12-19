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
import io.openepcis.model.epcis.VocabularyList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class VocabularyListES {
  private List<VocabularyElementsES> vocabularyElements;

  public VocabularyListES(final VocabularyList vocabularyList) {
    this.setVocabularyElements(
        vocabularyList.getVocabularyElements().stream()
            .map(vocabularyElementList -> new VocabularyElementsES(vocabularyElementList))
            .collect(Collectors.toList()));
  }

  @JsonIgnore
  public VocabularyList getCoreModel() {
    return VocabularyList.builder()
        .vocabularyElements(
            this.getVocabularyElements().stream()
                .map(VocabularyElementsES::getCoreModel)
                .collect(Collectors.toList()))
        .build();
  }
}
