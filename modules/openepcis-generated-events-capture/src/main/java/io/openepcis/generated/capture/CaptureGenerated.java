package io.openepcis.generated.capture;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.openepcis.client.rest.LowLevelEPCISClient;
import io.openepcis.testdata.generator.EPCISEventGenerator;
import io.openepcis.testdata.generator.constants.TestDataGeneratorException;
import io.openepcis.testdata.generator.reactivestreams.StreamingEPCISDocument;
import io.openepcis.testdata.generator.reactivestreams.StreamingEPCISDocumentOutput;
import io.openepcis.testdata.generator.template.InputTemplate;
import io.quarkus.logging.Log;
import io.quarkus.runtime.QuarkusApplication;
import io.quarkus.runtime.annotations.QuarkusMain;
import jakarta.enterprise.context.control.ActivateRequestContext;
import jakarta.inject.Inject;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.apache.commons.cli.*;
import org.eclipse.microprofile.context.ManagedExecutor;

import java.io.*;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

@QuarkusMain
public class CaptureGenerated implements QuarkusApplication {

    @Inject
    LowLevelEPCISClient epcisClient;
    @Inject
    JinjaUtil jinjaUtil;
    @Inject
    ObjectMapper objectMapper;
    @Inject
    ManagedExecutor managedExecutor;

    @Override
    @ActivateRequestContext
    public int run(final String... args) {
        // Define options
        final Options options = createCommandLineOptions();

        // define parser
        CommandLine cmd;
        final CommandLineParser parser = new DefaultParser();
        final HelpFormatter helper = new HelpFormatter();

        try {
            cmd = parser.parse(options, args);

            // Validate required options (file and either template or template-config)
            if (!isValidOptions(cmd)) {
                Log.error("Required file does not exist, Please check the path.");
                return -1;
            }

            // Check if both template and template-config options are provided
            if (cmd.hasOption("t") && cmd.hasOption("tc")) {

                // Render template and deserialize InputTemplate
                final String templatePath = cmd.getOptionValue("t");
                final String dataPath = cmd.getOptionValue("tc");
                final boolean useTempFile = cmd.hasOption("T");
                Log.debug("Should store to temporary file and use it for capture : " + useTempFile);

                // Validate both the file
                if (isInvalidFile(templatePath) || isInvalidFile(dataPath)) {
                    return -1;
                }

                final String renderedTemplate = jinjaUtil.renderInputTemplate(templatePath, dataPath);
                Log.debug("Generation of the InputTemplate from Jinja template completed.");
                Log.debug(renderedTemplate);

                // If there are no error during deserialization of the JSON then continue with execution.
                try {
                    // Create the object from generated InputTemplate
                    Log.debug("Generating the StreamingEPCISDocument from the rendered template...");
                    final StreamingEPCISDocument streamingEPCISDocument = generateTestEpcisEvents(renderedTemplate);
                    Log.debug("Successfully generated StreamingEPCISDocument.");

                    // If tf value is provided then write to temporary file and send it
                    if (useTempFile) {
                        Log.debug("Using temporary file approach for EPCIS capture.");
                        final File tempFile = File.createTempFile("epcis-document", ".json");
                        Log.debug("Temporary file created in path : " + tempFile.getAbsolutePath());
                        Log.debug("Temporary file name : " + tempFile.getName());

                        streamingEPCISDocument.writeToOutputStream(builder ->
                        {
                            try {
                                return StreamingEPCISDocumentOutput.outputStreamBuilder()
                                        .executor(managedExecutor)
                                        .objectMapper(objectMapper)
                                        .outputStream(new FileOutputStream(tempFile)).build();
                            } catch (FileNotFoundException e) {
                                throw new RuntimeException(e);
                            }
                        });


                        Log.debug("Finished writing EPCIS document to temporary file.");

                        // Start the REST call asynchronously so that it reads from the piped input stream concurrently.
                        Log.debug("Initiating asynchronous capture call with PipedInputStream...");
                        final CompletableFuture<Void> captureFuture = new CompletableFuture<>();
                        epcisClient.capture().capturePostJsonLD(new FileInputStream(tempFile))
                                .subscribe().with(
                                        response -> {
                                            Log.debug("Capture response received: " + response);
                                            Log.debug("Capture returned status : " + response.getStatus());
                                            Log.info("Capture response headers: " + response.getHeaders());
                                            Log.info("Capture ID : " + response.getHeaders().get("Location"));
                                            captureFuture.complete(null);
                                        },
                                        throwable -> {
                                            Log.error("Capture operation failed with error: ", throwable);
                                            captureFuture.completeExceptionally(throwable);
                                        }
                                );

                        // Wait for both the writer AND the capture call to complete
                        Log.debug("Waiting for both writerFuture and captureFuture to complete...");
                        try {
                            CompletableFuture.allOf(captureFuture).get();
                            Log.info("Capture and writing both completed successfully.");
                        } catch (Exception e) {
                            Log.error("Either writing or capture failed:", e);
                        }

                        Log.info("EPCIS capture from temporary file completed.");
                        tempFile.deleteOnExit();
                    } else {
                        Log.debug("Using streaming approach for EPCIS capture.");
                        // Creating the piped input stream to store the generated events in Document and for capture
                        Log.debug("Piping the Test Data Generator events to Capture API using PipedOutputStream");
                        final PipedOutputStream pos = new PipedOutputStream();
                        final PipedInputStream pis = new PipedInputStream(pos);

                        // Start writing asynchronously
                        Log.debug("Starting asynchronous writer task...");
                        final CompletableFuture<Void> writerFuture = CompletableFuture.runAsync(() -> {
                            try {
                                Log.debug("Writer task is now writing data to the output stream...");
                                streamingEPCISDocument.writeToOutputStream(builder ->
                                        StreamingEPCISDocumentOutput.outputStreamBuilder()
                                                .executor(managedExecutor)
                                                .objectMapper(objectMapper)
                                                .outputStream(pos)
                                                .build());
                                Log.debug("Writer task finished writing data successfully.");
                            } catch (IOException e) {
                                Log.error("Writer task encountered an IOException while writing data: ", e);
                                throw new RuntimeException(e);
                            } finally {
                                try {
                                    pos.close();
                                    Log.debug("Closed PipedOutputStream in writer task.");
                                } catch (IOException ignored) {
                                    Log.debug("Ignoring exception while closing PipedOutputStream in writer task: " + ignored.getMessage());
                                }
                            }
                        }, managedExecutor);

                        // Start the REST call asynchronously so that it reads from the piped input stream concurrently.
                        Log.debug("Initiating asynchronous capture call with PipedInputStream...");
                        final CompletableFuture<Void> captureFuture = new CompletableFuture<>();
                        epcisClient.capture().capturePostJsonLD(pis)
                                .subscribe().with(
                                        response -> {
                                            Log.debug("Capture response received: " + response);
                                            Log.debug("Capture returned status : " + response.getStatus());
                                            Log.info("Capture response headers: " + response.getHeaders());
                                            Log.info("Capture ID : " + response.getHeaders().get("Location"));
                                            captureFuture.complete(null);
                                        },
                                        throwable -> {
                                            Log.error("Capture operation failed with error: ", throwable);
                                            captureFuture.completeExceptionally(throwable);
                                        }
                                );

                        // Wait for both the writer AND the capture call to complete
                        Log.debug("Waiting for both writerFuture and captureFuture to complete...");
                        try {
                            CompletableFuture.allOf(writerFuture, captureFuture).get();
                            Log.info("Capture and writing both completed successfully.");
                        } catch (Exception e) {
                            Log.error("Either writing or capture failed:", e);
                        }
                    }
                } catch (Exception exception) {
                    Log.error("Exception during the streaming/capture process: ", exception);
                    throw new TestDataGeneratorException(exception.getMessage(), exception);
                }

                return 0;
            } else if (cmd.hasOption("f")) {
                // Otherwise, if file option is provided, run the capture logic
                final String filePath = cmd.getOptionValue("f");

                // Validate both the file
                if (isInvalidFile(filePath)) {
                    return -1;
                }

                // AtomicBooleans to signal completion of asynchronous task.
                final AtomicBoolean captureDone = new AtomicBoolean(true);

                // Start the REST call asynchronously to read from the piped input stream concurrently.
                Log.info("Initiating EPCIS capture operation.");
                epcisClient.capture().capturePostJsonLD(new FileInputStream(filePath))
                        .subscribe().with(
                                response -> {
                                    Log.info("EPCIS capture operation subscribe successful.");
                                    Log.info("Response Status  : " + response.getStatus());
                                    Log.info("Response Headers : " + response.getHeaders());
                                    Log.info("Capture ID       : " + response.getStringHeaders().get("Location"));
                                    captureDone.set(false);
                                },
                                throwable -> {
                                    Log.error("EPCIS capture operation subscribe failed.", throwable);
                                    captureDone.set(false);
                                    throwable.printStackTrace();
                                }
                        );

                // wait until the capture task has signaled completion.
                Log.info("Waiting for EPCIS capture subscribe to complete.");
                while (captureDone.get()) {
                    Thread.yield();
                }

                Log.info("EPCIS capture operation completed.");
            } else {
                // Print usage if required options are not provided
                Log.error("Required options not provided.");
                helper.printHelp("Usage: java -jar <jarfile> -t <templateFile> -tc <dataFile> OR -f <captureFile>", options);
            }
        } catch (Exception e) {
            Log.error("ERROR: " + e.getMessage());
            helper.printHelp("Usage: java -jar <jarfile> -t <templateFile> -tc <dataFile> OR -f <captureFile>", options);
            return -1;
        }

        return 0;
    }

    private Options createCommandLineOptions() {
        final Options options = new Options();
        options.addOption("f", "file", true, "EPCISDocument to capture");
        options.addOption("t", "template", true, "Jinja2 Template File");
        options.addOption("tc", "template-config", true, "Jinja2 Config for running Template");
        options.addOption("T", "temporary-file", false, "Write events to temp file and send the temp file.");
        return options;
    }

    private StreamingEPCISDocument generateTestEpcisEvents(final String renderedTemplate) throws JsonProcessingException {
        Log.debug("Generating the EPCIS events now from generated rendered InputTemplate");

        final InputTemplate inputTemplate = objectMapper.readValue(renderedTemplate, InputTemplate.class);

        // Check for validation errors in InputTemplate
        final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
        final Set<ConstraintViolation<InputTemplate>> violations = validator.validate(inputTemplate);
        if (!violations.isEmpty()) {
            final String message = violations.stream()
                    .map(ConstraintViolation::getMessage)
                    .collect(Collectors.joining(", "));
            Log.error("Validation errors in Input Template: " + message);
        }

        // Prepare streaming document with generated events
        final StreamingEPCISDocument streamingEPCISDocument = new StreamingEPCISDocument();
        StreamingEPCISDocument.storeContextInfo(inputTemplate.getEvents());
        StreamingEPCISDocument.storeContextUrls(inputTemplate.getContextUrls());
        streamingEPCISDocument.setPrettyPrint(true);
        streamingEPCISDocument.setEpcisEvents(EPCISEventGenerator.generate(inputTemplate));

        Log.debug("Generation of StreamingEPCISDocument has been completed.");
        return streamingEPCISDocument;
    }

    // Validate CMD options
    private boolean isValidOptions(final CommandLine cmd) {
        return (cmd.hasOption("t") && cmd.hasOption("tc")) || cmd.hasOption("f");
    }

    // Validate file
    private boolean isInvalidFile(final String filePath) {
        final File file = new File(filePath);

        if (!file.exists()) {
            Log.error(filePath + " does not exist!");
            return true;
        }

        if (!file.isFile()) {
            Log.error(filePath + " is not a file!");
            return true;
        }

        return false;
    }
}