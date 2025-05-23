package io.openepcis.generated.capture;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.Resources;
import com.hubspot.jinjava.Jinjava;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

@ApplicationScoped
public class JinjaUtil {

    @Inject
    ObjectMapper objectMapper;

    @Inject
    Jinjava jinjava;

    public String renderInputTemplate(final String jinjaTemplate, final String jinjaData) throws IOException {
        Log.debug("Generating the Jinja Template from provided Data");
        final Path jinjaTemplatePath = Paths.get(jinjaTemplate);
        final Path jinjaDataPath = Paths.get(jinjaData);

        // Read the template and data files
        final String jinjaTemplateString = Resources.toString(jinjaTemplatePath.toUri().toURL(), StandardCharsets.UTF_8); // Read the template content
        final String jinjaDataString = Resources.toString(jinjaDataPath.toUri().toURL(), StandardCharsets.UTF_8); // Read the jinja data

        // Convert the jinjaData to Map<String, Object>
        final Map<String, Object> jinjaTemplateData = objectMapper.readValue(jinjaDataString, new TypeReference<>() {
        });

        // Return the rendered template
        return jinjava.render(jinjaTemplateString, jinjaTemplateData);
    }
}
