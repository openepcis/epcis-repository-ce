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

import io.openepcis.model.epcis.format.FormatPreference;
import java.io.Serializable;
import java.util.List;

public interface ESModel<T> extends Serializable {
  T getCoreModel();

  T getCoreModel(List<Object> context);

  T getCoreModel(FormatPreference preference);

  T getCoreModel(FormatPreference preference, List<Object> context);
}
