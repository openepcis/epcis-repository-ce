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
package io.openepcis.repository.util;

import io.openepcis.model.dto.InvalidEPCISEventInfo;

import java.util.Collections;
import java.util.List;

public class InvalidEventInfoUtil {
  public static List<InvalidEPCISEventInfo> condenseInvalidInfos(List<InvalidEPCISEventInfo> invalidEPCISEventInfos) {
      if (invalidEPCISEventInfos == null || invalidEPCISEventInfos.isEmpty()) {
          return Collections.emptyList();
      }

    return invalidEPCISEventInfos.stream()
        .peek(
            invalidEPCISEventInfo -> {
              List<Integer> sequenceOfInvalidEvents =
                  invalidEPCISEventInfos.stream()
                      .filter(i -> i.equals(invalidEPCISEventInfo))
                      .map(InvalidEPCISEventInfo::getSequenceInEPCISDoc)
                      .flatMap(List::stream)
                      .distinct()
                      .toList();
              invalidEPCISEventInfo.setSequenceInEPCISDoc(sequenceOfInvalidEvents);
            })
        .distinct()
        .toList();
  }
}
