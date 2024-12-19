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

import com.fasterxml.jackson.databind.ObjectMapper;
import io.openepcis.constants.EPCIS;
import io.openepcis.epc.translator.exception.ValidationException;
import io.openepcis.model.epcis.*;
import io.openepcis.model.epcis.exception.CaptureValidationException;
import io.openepcis.model.epcis.exception.ExceptionMessages;
import io.openepcis.model.epcis.format.FormatPreference;
import io.openepcis.model.epcis.util.DataTypeUtil;
import io.openepcis.repository.Constants;
import io.openepcis.repository.model.*;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;

import java.util.*;
import java.util.stream.Collectors;

import static io.openepcis.constants.EPCIS.EPCIS_DEFAULT_NAMESPACES;

public class EventConvertor {

  private EventConvertor() {
    // There should be no need to create object of this class as this Utility class
    // contains a bunch of Utility methods.
  }

  public static EPCISEventES getESRepresentation(EPCISEvent event, Map<String, Object> metadata)
      throws ValidationException {
    return getESRepresentation(event, metadata, event.getContextInfo());
  }

  public static EPCISEventES getESRepresentation(EPCISEvent event, Map<String, Object> metadata, List<Object> context)
          throws ValidationException {
    if (event instanceof AggregationEvent)
      return new AggregationEventES((AggregationEvent) event, metadata, context);
    else if (event instanceof AssociationEvent)
      return new AssociationEventES((AssociationEvent) event, metadata, context);
    else if (event instanceof ObjectEvent)
      return new ObjectEventES((ObjectEvent) event, metadata, context);
    else if (event instanceof TransformationEvent)
      return new TransformationEventES((TransformationEvent) event, metadata, context);
    else if (event instanceof TransactionEvent)
      return new TransactionEventES((TransactionEvent) event, metadata, context);
    else return new EPCISEventES(event, metadata, context);
  }

  public static <Y extends ESModel<T>, T> T getCoreModel(Y object) {
    return object != null ? object.getCoreModel() : null;
  }

  public static <Y extends ESModel<T>, T> T getCoreModel(Y object, List<Object> context) {
    return object != null ? object.getCoreModel(context) : null;
  }

  public static <Y extends ESModel<T>, T> T getCoreModel(
      Y object, FormatPreference preference, List<Object> context) {
    return object != null ? object.getCoreModel(preference, context) : null;
  }

  public static <Y extends ESModel<T>, T> List<T> getCoreModel(List<Y> list, List<Object> context) {
    if (CollectionUtils.isNotEmpty(list)) {
      return list.stream().map(item -> item.getCoreModel(context)).collect(Collectors.toList());
    }
    return null;
  }

  public static <Y extends ESModel<T>, T> List<T> getCoreModel(List<Y> list) {
    if (CollectionUtils.isNotEmpty(list)) {
      return list.stream().map(ESModel::getCoreModel).collect(Collectors.toList());
    }
    return null;
  }

  public static <Y extends ESModel<T>, T> List<T> getCoreModel(
      List<Y> list, FormatPreference preference, List<Object> context) {
    if (CollectionUtils.isNotEmpty(list)) {
      return list.stream()
          .map(l -> l.getCoreModel(preference, context))
          .collect(Collectors.toList());
    }
    return null;
  }

  public static Map<String, Object> getCoreModelUserExtensions(
      List<Map<String, Object>> userExtensions) {
    if (CollectionUtils.isNotEmpty(userExtensions)) {
      return userExtensions.stream()
              .filter(Objects::nonNull)
              .filter(m -> m.get(Constants.KEY) != null)
              .filter(m -> m.get(getValueKey(m)) != null)
          .collect(
              Collectors.toMap(
                  map -> map.get(Constants.KEY).toString(),
                  map -> map.get(getValueKey(map)),
                  (a, b) -> a));
    }
    return null;
  }

  public static String getValueKey(Map<String, Object> userExtension) {
    if (userExtension.containsKey(Constants.LONG_VALUE)) return Constants.LONG_VALUE;
    else if (userExtension.containsKey(Constants.BOOLEAN_VALUE)) return Constants.BOOLEAN_VALUE;
    else if (userExtension.containsKey(Constants.DOUBLE_VALUE)) return Constants.DOUBLE_VALUE;
    else if (userExtension.containsKey(Constants.DATE_VALUE)) return Constants.DATE_VALUE;
    else if (userExtension.containsKey(Constants.KEYWORD_VALUE)) return Constants.KEYWORD_VALUE;
    else return Constants.OBJECT_VALUE;
  }

  public static List<Map<String, Object>> getUserExtensionsFromCoreModel(
      List<Object> context, Map<String, Object> userExtensions) {
    if (MapUtils.isNotEmpty(userExtensions)) {
      return userExtensions.entrySet().stream()
          .map(
              entry ->

                Map.of(
                    Constants.PATH,
                    getExpandedPathForUserExtension(context, entry.getKey()),
                    Constants.KEY,
                    entry.getKey().substring(entry.getKey().lastIndexOf(".") + 1),
                    Constants.EXPANDED_KEY,
                    getExpandedKeyForUserExtension(
                        context,
                        entry.getKey().contains(":")
                            ? entry
                                .getKey()
                                .substring(entry.getKey().lastIndexOf(".") + 1)
                                .split(":")
                            : new String[] {entry.getKey()}),
                    DataTypeUtil.getValueKeyFromType(entry.getValue()).concat(Constants.VALUE),
                    entry.getValue()))
          .collect(Collectors.toList());
    }
    return null;
  }

  public static String getExpandedKeyForUserExtension(
      final List<Object> context, final String[] field) {
    // Check if the Context contains empty namespaces map
    if (Objects.nonNull(context)) {
      boolean containsEmptyMap =
              context.stream()
                      .filter(obj -> obj instanceof Map)
                      .map(obj -> (Map<?, ?>) obj)
                      .anyMatch(Map::isEmpty);
      if (!containsEmptyMap && isNotExtensionField(field)) {
        final JSONObject jsonContext = JSONUtil.asJSONObject(convertContextToMap(context));
        final JSONObject defaultContext = JSONUtil.asJSONObject(EPCIS_DEFAULT_NAMESPACES);

        //Get the value corresponding to prefix from either user-defined context or EPCIS default context
        final String namespace = jsonContext.optString(field[0], defaultContext.optString(field[0]));
        if (!StringUtils.isBlank(namespace)) {
          return namespace + (namespace.endsWith("/") ? "" : "/") + field[1];
        } else {
          throw new CaptureValidationException(ExceptionMessages.EXPANDED_JSONLD_DOCUMENT_DOESN_T_CONTAIN_VALID_NAMESPACE);
        }
      }
    }
    return field[0];
  }

  public static String getExpandedKeyForUserExtension(final Map<String, Object> context, final String[] field) {
    final String namespace = context.containsKey(field[0]) ? context.get(field[0]).toString() : EPCIS_DEFAULT_NAMESPACES.getOrDefault(field[0], "").toString();

    //Get the namespace from either user-defined namespaces or default EPCIS namespaces
    if (!StringUtils.isBlank(namespace)) {
      return namespace.endsWith("/") ? namespace.concat(field[1]) : namespace.concat("/").concat(field[1]);
    } else {
      throw new CaptureValidationException(ExceptionMessages.EXPANDED_JSONLD_DOCUMENT_DOESN_T_CONTAIN_VALID_NAMESPACE);
    }
  }

  public static String getExpandedPathForUserExtension(final List<Object> context, String path) {
    final JSONObject jsonContext = JSONUtil.asJSONObject(convertContextToMap(context));
    final JSONObject defaultContext = JSONUtil.asJSONObject(EPCIS_DEFAULT_NAMESPACES);
    final String[] arr = StringUtils.substringsBetween(path, ".", ":");

    if (arr != null) {
      for (String a : arr) {
        if (a.contains(".")) {
          a = a.substring(a.indexOf(".") + 1);
        }

        //Get the namespace from user-defined or default context based on it form the path
        final String namespace = jsonContext.optString(a, defaultContext.optString(a));
        if (!StringUtils.isBlank(namespace)) {
          path = path.replace(a + ":", namespace.endsWith("/") ? namespace : namespace + "/");
        } else {
          throw new CaptureValidationException(ExceptionMessages.EXPANDED_JSONLD_DOCUMENT_DOESN_T_CONTAIN_VALID_NAMESPACE);
        }
      }
    }
    return path;
  }

  public static String getExpandedPathForUserExtension(
      final Map<String, Object> context, String path) {
    final String[] arr = StringUtils.substringsBetween(path, ".", ":");
    for (String a : arr) {
      if (context.containsKey(a)) {
        if (context.get(a).toString().endsWith("/")) {
          path = path.replace(a + ":", context.get(a).toString());
        } else {
          path = path.replace(a + ":", context.get(a).toString().concat("/"));
        }

      } else {
        throw new CaptureValidationException(
            ExceptionMessages.EXPANDED_JSONLD_DOCUMENT_DOESN_T_CONTAIN_VALID_NAMESPACE);
      }
    }
    return path;
  }

  public static void mapUserExtensionToDataType(
      Map<String, Object> context,
      Map<String, Object> userExtensions,
      Set<UserExtensionField> newFields) {
    final Date currentDate = new Date();
    userExtensions.forEach(
        (k, v) -> {
          String expandedKey =
              getExpandedKeyForUserExtension(
                  context, k.substring(k.lastIndexOf(".") + 1).split(":"));
          newFields.add(
              new UserExtensionField(
                  expandedKey, DataTypeUtil.getValueKeyFromType(v), currentDate));
        });
  }

  public static HashMap<String, Object> convertContextToMap(List<Object> context) {
    if (context == null || context.isEmpty()) {
      return new HashMap<>();
    }

    return context.stream()
        .map(m -> new ObjectMapper().convertValue(m, HashMap.class))
        .toList()
        .stream()
        .reduce(
            (firstMap, secondMap) -> {
              firstMap.putAll(secondMap);
              return firstMap;
            })
        .orElse(null);
  }

  private static boolean isNotExtensionField(String[] field) {
    return Objects.nonNull(field) && field.length > 1 && !EPCIS.EXTENSION.equals(field[0]);
  }
}
