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

import jakarta.inject.Inject;

import java.time.*;

public class RollingIndexUtil {

  private RollingIndexUtil() {
    /*
     There should be no need to create object of this class as this Utility class
     contains a bunch of Utility methods.
    */
    throw new UnsupportedOperationException("Invalid invocation of constructor");
  }

  public static String getRollingIndexName(String indexPrefix, Instant recordTime) {
    final LocalDateTime dateTime = LocalDateTime.ofInstant(recordTime, ZoneId.systemDefault());
    return indexPrefix
        + "-"
        + dateTime.getYear()
        + "-"
        + String.format("%02d", dateTime.getMonthValue());
  }

  public static String getRollingIndexName(String indexPrefix, ZonedDateTime dateTime, String orgName) {
    return indexPrefix
        + "-"
        + orgName
        + "-"
        + dateTime.getYear()
        + "-"
        + String.format("%02d", dateTime.getMonthValue());
  }

  public static String getRollingIndexName(String indexPrefix, LocalDateTime dateTime) {
    return indexPrefix
        + "-"
        + dateTime.getYear()
        + "-"
        + String.format("%02d", dateTime.getMonthValue());
  }

  public static String getRollingIndexName(String indexPrefix, OffsetDateTime dateTime,String orgName) {
    return indexPrefix
        + "-"
        + orgName
        + "-"
        + dateTime.getYear()
        + "-"
        + String.format("%02d", dateTime.getMonthValue());
  }

  public static String getIndexName(String indexPrefix,String orgName) {
    return indexPrefix
            + "-"
            + orgName;
  }
}
