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
package io.openepcis.client.deployment;

import io.openepcis.client.EPCISClientHealthCheck;
import io.openepcis.client.rest.LowLevelEPCISClient;
import io.openepcis.client.websocket.WebSocketClient;
import io.openepcis.quarkus.deployment.model.OpenEPCISBuildTimeConfig;
import io.quarkus.arc.deployment.AdditionalBeanBuildItem;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.builditem.FeatureBuildItem;
import io.quarkus.deployment.builditem.nativeimage.NativeImageResourcePatternsBuildItem;
import io.quarkus.smallrye.health.deployment.spi.HealthBuildItem;

public class EPCISClientProcessor {

  private static final String FEATURE = "openepcis-epcis-client";


  @BuildStep
  FeatureBuildItem feature() {
    return new FeatureBuildItem(FEATURE);
  }

  @BuildStep
  HealthBuildItem addHealthCheck(OpenEPCISBuildTimeConfig buildTimeConfig) {
    return new HealthBuildItem(EPCISClientHealthCheck.class.getName(),
            buildTimeConfig.healthEnabled());
  }

  @BuildStep()
  AdditionalBeanBuildItem buildEPCISClient() {
    return AdditionalBeanBuildItem.unremovableOf(LowLevelEPCISClient.class);
  }

  @BuildStep()
  AdditionalBeanBuildItem buildWebSocketClient() {
    return AdditionalBeanBuildItem.unremovableOf(WebSocketClient.class);
  }

  @BuildStep
  NativeImageResourcePatternsBuildItem addNativeImageResourceBuildItem() {
    return NativeImageResourcePatternsBuildItem.builder().includeGlobs(
            "META-INF/services/.*"
    ).build();
  }

}
