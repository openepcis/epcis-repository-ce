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
import io.openepcis.model.epcis.VocabularyElement;
import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class VocabularyElementES {
  private URI id;
  private List<AttributeES> attributes;
  private List<URI> children;

  public VocabularyElementES(final VocabularyElement vocabularyElement) {
    this.setId(vocabularyElement.getId());
    this.setAttributes(
        vocabularyElement.getAttributes().stream()
            .map(AttributeES::new)
            .collect(Collectors.toList()));
    this.setChildren(vocabularyElement.getChildren());
  }

  @JsonIgnore
  public VocabularyElement getCoreModel() {
    return VocabularyElement.builder()
        .id(this.getId())
        .attributes(
            CollectionUtils.isNotEmpty(this.getAttributes())
                ? this.getAttributes().stream()
                    .map(AttributeES::getCoreModel)
                    .collect(Collectors.toList())
                : null)
        .children(CollectionUtils.isNotEmpty(this.getChildren()) ? this.getChildren() : null)
        .build();
  }
}
