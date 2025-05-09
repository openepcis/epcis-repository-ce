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
package io.openepcis.quarkus.ce;

import io.quarkus.security.credential.PasswordCredential;
import io.quarkus.security.identity.IdentityProviderManager;
import io.quarkus.security.identity.SecurityIdentity;
import io.quarkus.security.runtime.QuarkusPrincipal;
import io.quarkus.security.runtime.QuarkusSecurityIdentity;
import io.quarkus.vertx.http.runtime.security.ChallengeData;
import io.quarkus.vertx.http.runtime.security.HttpAuthenticationMechanism;
import io.smallrye.mutiny.Uni;
import io.vertx.ext.web.RoutingContext;
import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Alternative;
import lombok.extern.slf4j.Slf4j;

import java.util.Set;

@Alternative
@ApplicationScoped
@Priority(1)
@Slf4j
public class AnonAuthMechanism implements HttpAuthenticationMechanism {

  private static final Set<String> ROLES = Set.of("admim", "capture", "query");
  private static final SecurityIdentity IDENTITY = QuarkusSecurityIdentity.builder().
          setPrincipal(new QuarkusPrincipal("admin"))
          .addRoles(ROLES)
          .addPermissionsAsString(ROLES)
          .addCredential(new PasswordCredential("password".toCharArray()))
          .build();

  @Override
  public Uni<SecurityIdentity> authenticate(
          RoutingContext context, IdentityProviderManager identityProviderManager) {
    return Uni.createFrom().item(IDENTITY);
  }

  @Override
  public Uni<ChallengeData> getChallenge(RoutingContext context) {
    return Uni.createFrom().item(new ChallengeData(400, "Authorization", "Bearer"));
  }


}
