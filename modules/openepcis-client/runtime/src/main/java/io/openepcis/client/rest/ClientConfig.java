package io.openepcis.client.rest;

import io.quarkus.runtime.annotations.ConfigPhase;
import io.quarkus.runtime.annotations.ConfigRoot;
import io.smallrye.config.ConfigMapping;

import java.util.Optional;

/**
 * ClientConfig defines the configuration properties for the OpenEPCIS client at runtime.
 * This interface uses Quarkus and SmallRye Config to map configuration properties defined in the application configuration file.
 * <p>
 * Configuration properties are prefixed with `openepcis-client` and follow a kebab-case naming strategy.
 * <p>
 * Example configuration in `application.properties`:
 * <pre>
 * openepcis-client.username=my-username
 * openepcis-client.password=my-password
 * openepcis-client.api-key=my-api-key
 * openepcis-client.api-key-secret=my-api-key-secret
 * </pre>
 */
@ConfigRoot(phase = ConfigPhase.RUN_TIME)
@ConfigMapping(prefix = "openepcis-client", namingStrategy = ConfigMapping.NamingStrategy.KEBAB_CASE)
public interface ClientConfig {

  /**
   * @return The optional username for basic authentication.
   */
  Optional<String> username();

  /**
   * @return The optional password for basic authentication.
   */
  Optional<String> password();

  /**
   * @return The optional API key for authentication.
   */
  Optional<String> apiKey();

  /**
   * @return The optional API key secret for authentication.
   */
  Optional<String> apiKeySecret();
}
