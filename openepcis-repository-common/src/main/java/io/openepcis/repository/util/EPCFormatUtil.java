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

import io.openepcis.epc.translator.StandardVocabConvertorUtil;
import io.openepcis.epc.translator.exception.UnsupportedGS1IdentifierException;
import io.openepcis.epc.translator.exception.UrnDLTransformationException;
import io.openepcis.epc.translator.exception.ValidationException;
import io.openepcis.epc.translator.util.ConverterUtil;
import io.openepcis.model.epcis.format.EPCFormat;
import io.openepcis.repository.model.EpcES;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

@Slf4j
public class EPCFormatUtil {

  private EPCFormatUtil() {
    throw new UnsupportedOperationException("Invalid invocation of constructor");
  }

  public static List<EpcES> buildListOfEpcES(List<String> epcList, List<Object> context)
      throws ValidationException {
    if (CollectionUtils.isNotEmpty(epcList)) {
      List<EpcES> listOfEpcES = new ArrayList();
      for (String epc : epcList) listOfEpcES.add(EPCFormatUtil.buildEpc(epc, context));

      return listOfEpcES;
    } else {
      return null;
    }
  }

  public static EpcES buildEpc(String epcAsCaptured) throws ValidationException {
    if (StringUtils.isNotEmpty(epcAsCaptured)) {
      final EpcES epc = new EpcES();
      epc.setAsCaptured(epcAsCaptured);
      if (epcAsCaptured.startsWith("urn:")) {
        epc.setAsURN(epcAsCaptured);
        epc.setCanonicalDL(ConverterUtil.toURI(epcAsCaptured));
      } else {
        // TODO: improve unsupported gs1 extension
        try {
          final Map<String, String> map = ConverterUtil.toURN(epcAsCaptured);
          epc.setAsURN(map.get("asURN"));
          epc.setCanonicalDL(map.get("canonicalDL"));
        } catch (UnsupportedGS1IdentifierException e) {
          epc.setAsCaptured(epcAsCaptured);
          if (epcAsCaptured.startsWith("http://") || epcAsCaptured.startsWith("https://")) {
            epc.setCanonicalDL(epcAsCaptured);
          }
        }
      }
      return epc;
    }
    return null;
  }

  public static EpcES buildEpc(String epcAsCaptured, List<Object> context) {
    if (StringUtils.isNotEmpty(epcAsCaptured)) {
      if (isExtensionValue(epcAsCaptured, context)) {
        final EpcES epc = new EpcES();
        epc.setAsCaptured(epcAsCaptured);
        return epc;
      } else {
        return buildEpc(epcAsCaptured);
      }
    }
    return null;
  }

  public static EpcES buildClassLevelEpc(String epcAsCaptured) throws ValidationException {
    if (StringUtils.isNotEmpty(epcAsCaptured)) {
      final EpcES epc = new EpcES();
      epc.setAsCaptured(epcAsCaptured);
      if (epcAsCaptured.startsWith("urn:")) {
        epc.setAsURN(epcAsCaptured);
        epc.setCanonicalDL(ConverterUtil.toURIForClassLevelIdentifier(epcAsCaptured));
      } else {
        final Map<String, String> map = ConverterUtil.toURNForClassLevelIdentifier(epcAsCaptured);
        epc.setAsURN(map.get("asURN"));
        epc.setCanonicalDL(map.get("canonicalDL"));
      }
      return epc;
    }
    return null;
  }

  public static EpcES buildClassLevelEpc(String epcAsCaptured, List<Object> context)
      throws ValidationException {
    if (StringUtils.isNotEmpty(epcAsCaptured)) {
      final EpcES epc = new EpcES();
      if (isExtensionValue(epcAsCaptured, context)) {
        epc.setAsCaptured(epcAsCaptured);
        return epc;
      } else {
        return buildClassLevelEpc(epcAsCaptured);
      }
    }
    return null;
  }

  public static List<EpcES> buildEpcESListForStandardVocab(List<String> epcList)
      throws ValidationException {
    if (CollectionUtils.isNotEmpty(epcList)) {
      List<EpcES> listOfEpcES = new ArrayList();
      for (String epc : epcList) listOfEpcES.add(EPCFormatUtil.buildEpcESForStandardVocab(epc));

      return listOfEpcES;
    } else {
      return null;
    }
  }

  public static EpcES buildEpcESForStandardVocab(String epcAsCaptured) throws ValidationException {
    if (StringUtils.isNotEmpty(epcAsCaptured)) {
      final EpcES epc = new EpcES();
      epc.setAsCaptured(epcAsCaptured);
      if (epcAsCaptured.startsWith("urn:") || epcAsCaptured.startsWith("gs1:")) {
        epc.setAsURN(epcAsCaptured);
        epc.setCanonicalDL(StandardVocabConvertorUtil.toURI(epcAsCaptured));

      } else {
        epc.setAsURN(StandardVocabConvertorUtil.toURN(epcAsCaptured));
        epc.setCanonicalDL(epcAsCaptured);
      }
      return epc;
    }
    return null;
  }

  public static List<String> getEpcAsCaptured(List<EpcES> epcList) {
    return CollectionUtils.isNotEmpty(epcList)
        ? epcList.stream().map(EpcES::getAsCaptured).collect(Collectors.toList())
        : null;
  }

  public static List<String> getEpcAsExpected(
      List<EpcES> epcList, String epcFormat, List<Object> context) {
    return CollectionUtils.isNotEmpty(epcList)
        ? epcList.stream()
            .map(e -> EPCFormatUtil.getEpcInExpectedFormat(e, epcFormat, context))
            .collect(Collectors.toList())
        : null;
  }

  public static URI getEpcAsCaptured(EpcES epc) {
    try {
      return Objects.nonNull(epc) ? new URI(epc.getAsCaptured()) : null;
    } catch (URISyntaxException e) {
      log.error("Error while converting epc: {} to URI", epc.getAsCaptured(), e);
      throw new UrnDLTransformationException(
          String.format("Error while converting DL{%s} to URN", epc.getAsCaptured()), e);
    }
  }

  public static URI getEpcAsExpected(EpcES epc, String epcFormat) {
    try {
      final EPCFormat format = EPCFormat.fromString(epcFormat).orElse(EPCFormat.No_Preference);
      if (Objects.nonNull(epc)) {
        return switch (format) {
          case Always_GS1_Digital_Link -> new URI(epc.getCanonicalDL());
          case Always_EPC_URN -> new URI(epc.getAsURN());
          case Never_Translates -> new URI(epc.getAsCaptured());
          default -> new URI(epc.getCanonicalDL() != null?epc.getCanonicalDL():epc.getAsCaptured());
        };
      } else {
        return null;
      }
    } catch (URISyntaxException e) {
      log.error("Error while converting epc: {} to URI", epc.getAsCaptured(), e);
      throw new UrnDLTransformationException(
          String.format("Error while converting captured{%s} to URI", epc.getAsCaptured()), e);
    } catch (RuntimeException e) {
      log.error("Error while converting epc: {} to requested {} format", epc.getAsCaptured(), epcFormat, e);
      throw new UrnDLTransformationException(
              String.format("Error while converting epc: captured{%s} to requested %s format", epc.getAsCaptured(), epcFormat), e);
    }
  }

  public static URI getEpcAsExpected(EpcES epc, String epcFormat, List<Object> context) {
    try {
      if (Objects.nonNull(epc)) {
        if (isExtensionValue(epc.getAsCaptured(), context)) {
          return new URI(epc.getAsCaptured());
        } else {
          return getEpcAsExpected(epc, epcFormat);
        }
      } else {
        return null;
      }
    } catch (URISyntaxException e) {
      log.error("Error while converting epc: {} to URI", epc.getAsCaptured(), e);
      throw new UrnDLTransformationException(
          String.format("Error while converting DL{%s} to URN", epc.getAsCaptured()), e);
    }
  }

  public static String getEpcInExpectedFormat(EpcES epc, String epcFormat) {
    if (Objects.nonNull(epc)) {
      if (epcFormat.equalsIgnoreCase("Always_GS1_Digital_Link")) {
        return epc.getCanonicalDL();
      } else if (epcFormat.equalsIgnoreCase("Always_EPC_URN")) {
        return epc.getAsURN();
      } else if (epcFormat.equalsIgnoreCase("Never_Translates")) {
        return epc.getAsCaptured();
      }
      return epc.getCanonicalDL();
    } else {
      return null;
    }
  }

  public static String getEpcInExpectedFormat(EpcES epc, String epcFormat, List<Object> context) {
    if (Objects.nonNull(epc)) {
      if (isExtensionValue(epc.getAsCaptured(), context)) {
        return epc.getAsCaptured();
      } else {
        return getEpcInExpectedFormat(epc, epcFormat);
      }
    } else {
      return null;
    }
  }

  private static boolean isExtensionValue(String epcAsCaptured, List<Object> context) {
    final Map<String, Object> contextMap = convertContextToMap(context);
    final List<String> prefixes = contextMap.keySet().stream().toList();
    final List<String> namespaces = contextMap.values().stream().map(Object::toString).toList();
    String prefix = epcAsCaptured.split(":")[0];
    if (prefixes.contains(prefix)) {
      return true;
    } else if (epcAsCaptured.startsWith("http://") || epcAsCaptured.startsWith("https://")) {
      String namespace = epcAsCaptured.substring(0, epcAsCaptured.lastIndexOf("/"));
      return namespaces.contains(namespace);
    } else {
      return false;
    }
  }
}
