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

import static io.openepcis.repository.util.EventConvertor.convertContextToMap;

import io.openepcis.constants.EPCIS;
import io.openepcis.epc.translator.StandardVocabConvertorUtil;
import io.openepcis.epc.translator.exception.ValidationException;
import io.openepcis.model.epcis.format.CBVFormat;
import io.openepcis.epc.translator.util.ConverterUtil;
import io.openepcis.repository.model.CbvES;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

@Slf4j
public class CBVFormatUtil {

  public static List<CbvES> buildCbvESListForStandardVocab(
      List<String> cbvList, String urnPrefix, List<Object> context) throws ValidationException {
    if (CollectionUtils.isNotEmpty(cbvList)) {
      List<CbvES> listOfCbvES = new ArrayList();
      for (String cbv : cbvList)
        listOfCbvES.add(CBVFormatUtil.buildCbvESForStandardVocab(cbv, urnPrefix, context));

      return listOfCbvES;
    } else {
      return null;
    }
  }

  public static CbvES buildCbvESForStandardVocab(String cbvAsCaptured, String urnPrefix)
      throws ValidationException {
    if (StringUtils.isNotEmpty(cbvAsCaptured)) {
      final CbvES cbv = new CbvES();

      cbv.setAsCaptured(cbvAsCaptured);

      if (cbvAsCaptured.startsWith("urn:") || cbvAsCaptured.startsWith("gs1:")) {
        cbv.setAsURN(cbvAsCaptured);
        cbv.setAsURI(StandardVocabConvertorUtil.toURI(cbvAsCaptured));
        cbv.setAsBareString(convertToSingleTerm(cbvAsCaptured, urnPrefix));
      } else if (cbvAsCaptured.startsWith(EPCIS.GS1_CBV_DOMAIN)
          || cbvAsCaptured.startsWith(EPCIS.GS1_VOC_DOMAIN)) {
        cbv.setAsURN(StandardVocabConvertorUtil.toURN(cbvAsCaptured));
        cbv.setAsURI(cbvAsCaptured);
        cbv.setAsBareString(ConverterUtil.toBareStringVocabulary(cbvAsCaptured));
      } else if (isURN(cbvAsCaptured)) {
        cbv.setAsURN(cbvAsCaptured);
      } else if (isWebURL(cbvAsCaptured)) {
        cbv.setAsURI(cbvAsCaptured);
      } else {
        cbv.setAsURN(urnPrefix.concat(cbvAsCaptured));
        cbv.setAsURI(StandardVocabConvertorUtil.toURI(cbv.getAsURN()));
        cbv.setAsBareString(cbvAsCaptured);
      }
      return cbv;
    }
    return null;
  }

  // Respects GS1-extensions and allows for custom vocabulary values
  public static CbvES buildCbvESForStandardVocab(
      String cbvAsCaptured, String urnPrefix, List<Object> context) throws ValidationException {
    if (StringUtils.isEmpty(cbvAsCaptured)) {
      return null;
    }
    if (isExtensionValue(cbvAsCaptured, context)) {
      final CbvES cbvES = new CbvES();
      cbvES.setAsCaptured(cbvAsCaptured);
      return cbvES;
    }
    return buildCbvESForStandardVocab(cbvAsCaptured, urnPrefix);
  }

  public static List<String> getCbvAsExpected(List<CbvES> cbvList, String cbvFormat) {
    return CollectionUtils.isNotEmpty(cbvList)
        ? cbvList.stream()
            .map(c -> CBVFormatUtil.getCbvInExpectedFormat(c, cbvFormat))
            .collect(Collectors.toList())
        : null;
  }

  public static List<String> getCbvAsExpected(
      List<CbvES> cbvList, String cbvFormat, List<Object> context) {
    return CollectionUtils.isNotEmpty(cbvList)
        ? cbvList.stream()
            .map(c -> CBVFormatUtil.getCbvInExpectedFormat(c, cbvFormat, context))
            .collect(Collectors.toList())
        : null;
  }

  public static String getCbvInExpectedFormat(CbvES cbv, String cbvFormat) {
    if (Objects.nonNull(cbv)) {
      if (StringUtils.isBlank(cbv.getAsURN())) {
        return cbv.getAsCaptured();
      } else if (cbvFormat.equalsIgnoreCase(CBVFormat.Always_Web_URI.name())) {
        return cbv.getAsURI();
      } else if (cbvFormat.equalsIgnoreCase(CBVFormat.Always_URN.name())) {
        return cbv.getAsURN();
      } else if (cbvFormat.equalsIgnoreCase(CBVFormat.Never_Translates.name())) {
        return cbv.getAsCaptured();
      }
      return cbv.getAsBareString();
    } else {
      return null;
    }
  }

  public static String getCbvInExpectedFormat(CbvES cbv, String cbvFormat, List<Object> context) {
    if (Objects.isNull(cbv)) {
      return null;
    }
    return isExtensionValue(cbv.getAsCaptured(), context)
        ? cbv.getAsCaptured()
        : getCbvInExpectedFormat(cbv, cbvFormat);
  }

  public static String convertToSingleTerm(String urn, String urnPrefix)
      throws ValidationException {
    return urn.substring(urnPrefix.length());
  }

  private static boolean isURN(String cbv) {
    return cbv.toLowerCase().startsWith("urn:");
  }
  private static boolean isWebURL(String cbv) {
    return cbv.toLowerCase().startsWith("https://") || cbv.toLowerCase().startsWith("http://");
  }

  private static boolean isExtensionValue(String cbvAsCaptured, List<Object> context) {
    final Map<String, Object> contextMap = convertContextToMap(context);
    final List<String> prefixes = contextMap.keySet().stream().toList();
    String prefix = cbvAsCaptured.split(":")[0];
    return prefixes.contains(prefix);
  }
}
