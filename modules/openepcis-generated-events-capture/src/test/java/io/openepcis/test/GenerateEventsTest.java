package io.openepcis.test;

import io.quarkus.test.junit.main.Launch;
import io.quarkus.test.junit.main.LaunchResult;
import io.quarkus.test.junit.main.QuarkusMainTest;
import org.junit.jupiter.api.Test;

import java.io.IOException;

@QuarkusMainTest
public class GenerateEventsTest {

    @Test
    public void testCaptureGenerated() throws Exception {
        String[] args = {"-t", "src/test/resources/template.json", "-tc", "src/test/resources/content.json"};
        //CaptureGenerated.main(args);
    }

    @Test
    @Launch({"-t", "src/test/resources/template.json", "-tc", "src/test/resources/content.json"})
    public void testTemplateRendering(LaunchResult result) throws IOException {
        //Assertions.assertEquals(0, result.exitCode());
     }

    @Test
    @Launch({"-f", "src/test/resources/epcisEvent.json"})
    public void directEpcisEvent(LaunchResult result) throws IOException {
        //Assertions.assertEquals(0, result.exitCode());
        System.out.println(result);
        //Assertions.assertTrue(output.contains("200 OK"));
    }
}
