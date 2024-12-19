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
import io.openepcis.model.epcis.BizTransactionList;
import io.openepcis.model.epcis.constants.CBVUrnPrefix;
import io.openepcis.model.epcis.format.CBVFormat;
import io.openepcis.model.epcis.format.FormatPreference;
import io.openepcis.repository.util.CBVFormatUtil;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BizTransactionListES implements ESModel<BizTransactionList> {

  private CbvES type;
  private String bizTransaction;

  public BizTransactionListES(final BizTransactionList bizTransactionList, List<Object> context) {
    this.setType(
        CBVFormatUtil.buildCbvESForStandardVocab(
            bizTransactionList.getType(), CBVUrnPrefix.BIZTRANSACTION, context));
    this.setBizTransaction(bizTransactionList.getBizTransaction());
  }

  @Override
  @JsonIgnore
  public BizTransactionList getCoreModel() {
    return BizTransactionList.builder()
        .type(CBVFormatUtil.getCbvInExpectedFormat(this.getType(), CBVFormat.No_Preference.name()))
        .bizTransaction(this.getBizTransaction())
        .build();
  }

  @Override
  public BizTransactionList getCoreModel(List<Object> context) {
    return BizTransactionList.builder()
        .type(
            CBVFormatUtil.getCbvInExpectedFormat(
                this.getType(), CBVFormat.No_Preference.name(), context))
        .bizTransaction(this.getBizTransaction())
        .build();
  }

  @Override
  public BizTransactionList getCoreModel(FormatPreference preference) {

    final String cbvFormat =
        preference.getCbvFormat() != null
            ? preference.getCbvFormat().name()
            : CBVFormat.No_Preference.name();

    return BizTransactionList.builder()
        .type(CBVFormatUtil.getCbvInExpectedFormat(this.getType(), cbvFormat))
        .bizTransaction(this.getBizTransaction())
        .build();
  }

  @Override
  public BizTransactionList getCoreModel(FormatPreference preference, List<Object> context) {
    final String cbvFormat =
        preference.getCbvFormat() != null
            ? preference.getCbvFormat().name()
            : CBVFormat.No_Preference.name();

    return BizTransactionList.builder()
        .type(CBVFormatUtil.getCbvInExpectedFormat(this.getType(), cbvFormat, context))
        .bizTransaction(this.getBizTransaction())
        .build();
  }
}
