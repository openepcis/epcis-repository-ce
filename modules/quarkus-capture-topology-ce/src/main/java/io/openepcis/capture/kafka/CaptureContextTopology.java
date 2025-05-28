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
package io.openepcis.capture.kafka;

import static io.openepcis.model.epcis.exception.ExceptionMessages.ERROR_WHILE_PERSISTING_EVENT;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.openepcis.capture.context.EPCISEventObjectNodeUtil;
import io.openepcis.capture.context.EventCountMessage;
import io.openepcis.capture.context.StorageService;
import io.openepcis.capture.context.message.DocumentCaptureMessage;
import io.openepcis.capture.context.message.EPCISValidationMessage;
import io.openepcis.capture.service.EPCISEventPersistenceService;
import io.openepcis.capture.service.EPCISEventValidationService;
import io.openepcis.eventhash.EventHashGenerator;
import io.openepcis.model.dto.CaptureJobStatusMessage;
import io.openepcis.model.dto.CaptureStatusMessage;
import io.openepcis.model.dto.InvalidEPCISEventInfo;
import io.openepcis.model.epcis.exception.PersistenceException;
import io.openepcis.opentelemetry.logging.OpenEPCISLogger;
import io.openepcis.repository.api.EventRepository;
import io.openepcis.repository.util.InvalidEventInfoUtil;
import io.openepcis.service.util.Constants;
import io.quarkus.kafka.client.serialization.ObjectMapperSerde;
import io.quarkus.logging.Log;
import io.smallrye.mutiny.Uni;
import io.smallrye.reactive.messaging.kafka.Record;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.*;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.*;
import org.apache.kafka.streams.state.KeyValueBytesStoreSupplier;
import org.apache.kafka.streams.state.Stores;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.eclipse.microprofile.reactive.messaging.OnOverflow;

/**
 * CaptureContextTopology is responsible for constructing and managing the Kafka Streams topology
 * that processes EPCIS event capture messages. It integrates various services such as validation,
 * persistence, storage, and Kafka configuration to perform the following tasks:
 *
 * <ul>
 *   <li>Consume EPCIS event capture messages from configured Kafka topics.
 *   <li>Aggregate and update capture job status messages with event counts and validation
 *       information.
 *   <li>Perform event validation, including schema checks, duplication, and integrity verification.
 *   <li>Handle external storage for large EPCIS events (e.g., events larger than 4KB).
 *   <li>Produce validation success and failure messages to appropriate Kafka topics.
 *   <li>Persist capture job statuses in a reactive repository and update associated systems.
 * </ul>
 */
@RequiredArgsConstructor
@ApplicationScoped
public class CaptureContextTopology {
  private static final OpenEPCISLogger log =
      OpenEPCISLogger.getLogger(CaptureContextTopology.class);
  private final EPCISEventValidationService epcisEventValidationService;
  private final EPCISEventPersistenceService epcisEventPersistenceService;
  private final EventRepository reactiveRepository;
  private final KafkaConfigurationService kafkaConfigurationService;
  private final EventHashGenerator eventHashGenerator = new EventHashGenerator();
  @Inject ObjectMapper objectMapper;
  @Inject StorageService storageService;

  // Emitter to produce into Topic: capture-document-event after validation/persistence with
  // valid/invalid entries from CaptureJobStatus
  @Channel("capture-document-event-out")
  @OnOverflow(OnOverflow.Strategy.UNBOUNDED_BUFFER)
  Emitter<Record<String, CaptureStatusMessage>> captureStatusMessageEmitter;

  // Emitter to produce into Topic: epcis-event-validated_success from validationStream for
  // successful validation of the event
  @Channel("epcis-event-validated-success-out")
  @OnOverflow(OnOverflow.Strategy.UNBOUNDED_BUFFER)
  Emitter<Record<String, EPCISValidationMessage>> eventValidatedSuccessEmitter;

  // Emitter to produce into Topic: epcis-event-validated_failure from validationStream for
  // unsuccessful validation (duplication/invalid date/info etc.)
  @Channel("epcis-event-validated-failure-out")
  @OnOverflow(OnOverflow.Strategy.UNBOUNDED_BUFFER)
  Emitter<Record<String, EPCISValidationMessage>> eventValidatedFailureEmitter;

  @Produces
  public Topology buildTopology() {
    final StreamsBuilder builder = new StreamsBuilder();

    final ObjectMapperSerde<CaptureJobStatusMessage> captureDataSerde =
        new ObjectMapperSerde<>(CaptureJobStatusMessage.class);
    final ObjectMapperSerde<CaptureStatusMessage> captureStatusSerde =
        new ObjectMapperSerde<>(CaptureStatusMessage.class);
    final ObjectMapperSerde<DocumentCaptureMessage> documentCapturedMessageSerde =
        new ObjectMapperSerde<>(DocumentCaptureMessage.class);
    final ObjectMapperSerde<EPCISValidationMessage> epcisEventValidationMessageSerde =
        new ObjectMapperSerde<>(EPCISValidationMessage.class);
    final ObjectMapperSerde<EventCountMessage> eventCountWithTracingSerde =
        new ObjectMapperSerde<>(EventCountMessage.class);

    final KeyValueBytesStoreSupplier storeSupplier =
        Stores.persistentKeyValueStore(kafkaConfigurationService.stores().captureDocsStore());

    final GlobalKTable<String, CaptureJobStatusMessage> docs =
        builder.globalTable(
            kafkaConfigurationService.topics().captureDocs(),
            Consumed.with(Serdes.String(), captureDataSerde),
            Materialized.as(kafkaConfigurationService.stores().globalCaptureJobMessageStore()));

    // Consume from Topic: capture-document-event-count Produced Channel:
    // capture-document-event-count-out for writing capturedEventCount
    var eventCountStream =
        builder.stream(
            kafkaConfigurationService.topics().captureDocEventCount(),
            Consumed.with(Serdes.String(), eventCountWithTracingSerde));

    // Consume from Topic: capture-document-event-count to write the capturedEventCount for the
    // CaptureJob and Produce to Topic: capture-document-event
    eventCountStream
        .map(
            (captureID, eventCountWithTracing) -> {
              final CaptureStatusMessage captureStatusMessage =
                  CaptureStatusMessage.eventCapturedCount(
                      eventCountWithTracing.getCount(), new HashMap<>());
              captureStatusMessage.setTraceId(eventCountWithTracing.getTraceId());
              captureStatusMessage.setSpanId(eventCountWithTracing.getSpanId());
              captureStatusMessage.setDefaultGroup(eventCountWithTracing.getDefaultGroup());
              return KeyValue.pair(captureID, captureStatusMessage);
            })
        .to(
            kafkaConfigurationService.topics().captureDocsEvent(),
            Produced.with(Serdes.String(), captureStatusSerde));

    // After capturedEventCount consume from Topic: capture-document-event and prepare
    // CaptureStatusMessage for the update info in joinedEventStatusStream
    final KStream<String, CaptureStatusMessage> eventStatusStream =
        builder.stream(
            kafkaConfigurationService.topics().captureDocsEvent(),
            Consumed.with(Serdes.String(), captureStatusSerde));
    eventStatusStream.foreach(
        (captureID, captureStatusMessage) -> {
            log.debug("event count {} = {}", captureID, captureStatusMessage);
        });

    // Build CaptureJobStatusMessage with Captured/Processed/Valid/Invalid event count information.
    // This is triggered automatically when information produced to Topic: capture-document-event
    // during Captured/Valid/Invalid/Processed flow.
    final KStream<String, CaptureJobStatusMessage> joinedEventStatusStream =
        eventStatusStream
            .join(
                docs,
                (captureID, captureStatus) -> captureID,
                (captureID, captureStatus, captureJobStatus) ->
                    new CaptureJobStatusMessageAggregation(
                        captureJobStatus, captureStatus, epcisEventPersistenceService))
            .groupByKey()
            .aggregate(
                CaptureJobStatusMessage::new,
                (captureID, aggregation, captureJobStatusMessage) ->
                    aggregation
                        .update(captureJobStatusMessage)
                        .chain(
                            msg -> {
                                log.debug("aggregating from {}", msg);
                                msg.setErrors(
                                    InvalidEventInfoUtil.condenseInvalidInfos(msg.getErrors()));
                                return Uni.createFrom().item(msg);
                            })
                        .subscribe()
                        .asCompletionStage()
                        .join(),
                Materialized.<String, CaptureJobStatusMessage>as(storeSupplier)
                    .withKeySerde(Serdes.String())
                    .withValueSerde(captureDataSerde))
            .toStream();

    // Finally update the captureJobStatus (running/success) in OpenSearch/ElasticSearch repository
    // with count information (valid/invalid/processed/capture, etc.)
    joinedEventStatusStream
        .filter((captureID, captureJobStatus) -> captureJobStatus.getFinishedAt() != null)
        .foreach(
            (captureID, captureJobStatusMessage) -> {

              try  {
                if (captureJobStatusMessage.getInvalidEventCount() > 0
                    && captureJobStatusMessage.isRollback()) {
                  epcisEventPersistenceService
                      .removeEventsForCaptureID(
                          captureJobStatusMessage.getCaptureID(),
                          captureJobStatusMessage.getCreatedAt(),
                          captureJobStatusMessage.getDefaultGroup())
                      .subscribe()
                      .with(
                          aBoolean -> {
                            log.debug(
                                "successfully deleted events with the captureId: {}",
                                captureJobStatusMessage.getCaptureID());
                          },
                          throwable -> {
                            log.error(
                                "Error while deleting the events with the captureId: {}",
                                captureJobStatusMessage.getCaptureID(),
                                throwable);
                          });

                  log.debug(
                      "EPCIS document with captureId={} was not processed due to event invalid event present in document and "
                          + "GS1-Capture-Error-Behaviour=rollback",
                      captureID);
                }
                if (captureJobStatusMessage.getCapturedEventCount()
                    > captureJobStatusMessage.getProcessedEventCount()) {
                  captureJobStatusMessage.setCapturedEventCount(
                      captureJobStatusMessage.getProcessedEventCount());
                  captureJobStatusMessage.setFinishedAt(OffsetDateTime.now());
                  captureJobStatusMessage.setSuccess(captureJobStatusMessage.getErrors().isEmpty());
                  captureJobStatusMessage.setRunning(false);
                }

                // Save the capture job status and information and if any exception occurs then
                // record it on span
                reactiveRepository
                    .saveCaptureJob(
                        captureJobStatusMessage,
                        captureID,
                        captureJobStatusMessage.getCreatedAt(),
                        captureJobStatusMessage.getDefaultGroup(),
                        captureJobStatusMessage.getMetadata())
                    .onFailure()
                    .invoke(
                        failure -> {
                          log.error(
                              "Error occurred during the saving of the capture job : {}",
                              failure.getMessage());
                        })
                    .subscribe()
                    .with(
                        result -> {
                          // Capture job was successfully saved
                          log.debug("Successfully saved capture job for captureID: {}", captureID);
                        },
                        throwable -> {
                          // Capture job saving was failed
                          log.debug(
                              "Failed to save capture job for captureID : {}",
                              captureID,
                              throwable.getMessage());
                        });
              } catch (Exception e) {
                log.error(e.getMessage(), e);
              }
            });
    joinedEventStatusStream.to(
        kafkaConfigurationService.topics().captureDocs(),
        Produced.with(Serdes.String(), captureDataSerde));

    // Validation stream to validate the event again schema/duplicate/invalid entries and write to
    // Topics (epcis-event-validated_success/epcis-event-validated_failure)
    // On Validation Success produce to Topic: epcis-event-validated_success Channel:
    // epcis-event-validated-success-out
    // On Validation Failure produce to Topic: epcis-event-validated_failure Channel:
    // epcis-event-validated-failure-out
    final KStream<String, EPCISValidationMessage> validationStream =
        builder.stream(
                kafkaConfigurationService.topics().epcisEventCaptured(),
                Consumed.with(Serdes.String(), documentCapturedMessageSerde))
            .map(
                (captureID, documentCaptureMessage) -> {

                  try  {

                    ObjectNode singleEventNode; // Retrieve event from storage
                    final String[] arrayOfHash; // Generate hashes for verification

                    // If EPCIS Event is stored in S3/Azure Blob Storage (>4KB) then get the event
                    // from storage and process
                    if (StringUtils.isBlank(documentCaptureMessage.getObjectNodeString())) {
                      log.debug(
                          "Large event (>4kb) stored in StorageService {}",
                          documentCaptureMessage.getStorageKey());
                      final InputStream originalStream =
                          storageService.get(documentCaptureMessage.getEventStorageKey());
                      final byte[] content =
                          originalStream.readAllBytes(); // Read the entire stream into byte array

                      // Create new InputStreams from the buffered content
                      final InputStream eventStream = new ByteArrayInputStream(content);

                      arrayOfHash =
                          eventHashGenerator
                              .fromJson(eventStream, "sha-256")
                              .subscribe()
                              .asStream()
                              .toList()
                              .toArray(new String[0]);

                      eventStream.reset();

                      singleEventNode = (ObjectNode) objectMapper.readTree(eventStream);
                    } else {
                      // If event below 4kb and stored as string in documentCaptureMessage then read
                      // and continue as before
                      log.debug(
                          "Small event (<4kb) stored as string in documentCaptureMessage ObjectNodeString");

                      final String epcisDocumentString =
                          documentCaptureMessage.getObjectNodeString();
                      singleEventNode = (ObjectNode) objectMapper.readTree(epcisDocumentString);
                      arrayOfHash =
                          eventHashGenerator
                              .fromJson(
                                  new ByteArrayInputStream(
                                      epcisDocumentString.getBytes(StandardCharsets.UTF_8)),
                                  "sha-256")
                              .subscribe()
                              .asStream()
                              .toList()
                              .toArray(new String[0]);
                    }

                    // Extract event and context
                    final Pair<ObjectNode, Map<String, Object>> eventAndContextPair =
                        EPCISEventObjectNodeUtil.extractEventAndContextNodeFromDocument(
                            singleEventNode, objectMapper);
                    final Map<String, Object> contextAsMap = eventAndContextPair.getValue();
                    final ObjectNode eventNode = eventAndContextPair.getKey();

                    // Initialize validation info and add tags
                    final List<InvalidEPCISEventInfo> invalidEPCISEvents = new ArrayList<>();

                    // Validate the events against schema/duplication/invalid info's etc. and also
                    // generate event hash
                    epcisEventValidationService
                        .validateEvent(
                            singleEventNode,
                            eventNode,
                            arrayOfHash,
                            contextAsMap,
                            documentCaptureMessage.getCaptureID(),
                            invalidEPCISEvents,
                            documentCaptureMessage.getEventIndex(),
                            documentCaptureMessage.getEventIDs(),
                            eventNode.has(Constants.ERROR_DECLARATION),
                            documentCaptureMessage.getMetadata())
                        .subscribe()
                        .with(
                            item -> {
                              // Update the docCapMsg with singleEventNode containing the EventHash
                              // generated
                              final String eventWithHash = singleEventNode.toString();

                              // Define a common emitter logic as a Runnable to be run after
                              // completion of PUT or after updating the eventString with Hash
                              // This is to avoid sending success/failure before the event has been
                              // successfully PUT in S3/Blob
                              final Runnable emitValidationMessage =
                                  () -> {
                                    // After the successful event validation generate
                                    // EPCISValidationMessage
                                    final EPCISValidationMessage validationMessage =
                                        new EPCISValidationMessage();
                                    validationMessage.updateFrom(
                                        documentCaptureMessage, invalidEPCISEvents);

                                    // If no errors found during validation then produce
                                    // (channel:epcis-event-validated-success-out Topic:
                                    // epcis-event-validated_success)
                                    if (invalidEPCISEvents.isEmpty()) {
                                      Log.debug(
                                          " ✅ Event validation successful, No invalid information !!! ✅ ");
                                      eventValidatedSuccessEmitter.send(
                                          Record.of(captureID, validationMessage));
                                    } else {
                                      // If any errors found during validation then produce
                                      // (channel:epcis-event-validated-failure-out Topic:
                                      // epcis-event-validated_failure)
                                      Log.debug(
                                          " ❌ Event validation failed with invalid information !!! ❌ ");
                                      eventValidatedFailureEmitter.send(
                                          Record.of(captureID, validationMessage));
                                    }
                                  };

                              // Updates a large event in cloud storage (S3/Blob) with its
                              // associated hash.
                              if (StringUtils.isBlank(
                                  documentCaptureMessage.getObjectNodeString())) {
                                final byte[] eventWithHashBytes =
                                    eventWithHash.getBytes(StandardCharsets.UTF_8);
                                final InputStream eventWithHashStream =
                                    new ByteArrayInputStream(eventWithHashBytes);
                                final Map<String, String> eventStorageTags =
                                    documentCaptureMessage.getEventStorageTags();

                                storageService
                                    .put(
                                        documentCaptureMessage.getEventStorageKey(),
                                        eventStorageTags.get("Content_Type"),
                                        Optional.of((long) eventWithHashBytes.length),
                                        eventStorageTags,
                                        eventWithHashStream)
                                    .replaceWithVoid()
                                    .subscribe()
                                    .with(
                                        success -> {
                                          Log.debug(
                                              "Put event with Hash to StorageService completed for eventStorageKey : "
                                                  + documentCaptureMessage.getEventStorageKey());
                                          // Invoke the common emitter logic only after a successful
                                          // put
                                          emitValidationMessage.run();
                                        },
                                        failure -> {
                                          Log.error(
                                              "Put event with Hash to cloud storage failed for eventStorageKey : ",
                                              documentCaptureMessage.getEventStorageKey(),
                                              failure);
                                        });
                              } else {
                                // Update small events (< 4KB) directly in the
                                // DocumentCaptureMessage
                                documentCaptureMessage.setObjectNodeString(eventWithHash);
                                // Immediately emit since there's no asynchronous delay
                                emitValidationMessage.run();
                              }
                            },
                            failure -> {
                              // If any failure occurred during validation then produce
                              // (channel:epcis-event-validated-failure-out Topic:
                              // epcis-event-validated_failure)
                              log.info(
                                  " ❌ Event validation was failed due to : {} ❌ ",
                                  failure.getMessage(),
                                  documentCaptureMessage.getEventStorageKey());
                              final EPCISValidationMessage failureMessage =
                                  new EPCISValidationMessage();
                              failureMessage.updateFrom(documentCaptureMessage, invalidEPCISEvents);

                              // If validation fails then produce
                              // (channel:epcis-event-validated-failure-out Topic:
                              // epcis-event-validated_failure)
                              eventValidatedFailureEmitter.send(
                                  Record.of(captureID, failureMessage));
                            });

                    // Prepare final validation message
                    final EPCISValidationMessage msg = new EPCISValidationMessage();
                    msg.updateFrom(documentCaptureMessage, invalidEPCISEvents);

                    log.info(
                        "validation result for {} = {}",
                        documentCaptureMessage.getCaptureID(),
                        msg);
                    return KeyValue.pair(captureID, msg);
                  } catch (Exception e) {
                    // If any exception occur during the Event hash generation/validation then
                    // produce (channel:epcis-event-validated-failure-out Topic:
                    // epcis-event-validated_failure)
                    log.error(
                        " ❌ Exception occurred during validation/hash generation : {} ❌ ",
                        e,
                        documentCaptureMessage.getEventStorageKey());
                    final EPCISValidationMessage exceptionMessage = new EPCISValidationMessage();
                    exceptionMessage.updateFrom(
                        documentCaptureMessage,
                        List.of(
                            new InvalidEPCISEventInfo(
                                e.getClass().getSimpleName(),
                                e.getMessage(),
                                500,
                                List.of(e.getStackTrace()).toString(),
                                List.of(documentCaptureMessage.getEventIndex()))));
                    eventValidatedFailureEmitter.send(Record.of(captureID, exceptionMessage));

                    return KeyValue.pair(captureID, exceptionMessage);
                  } finally {
                  }
                });

    // If Validation unsuccessful in the above validationStream then consume from Topic:
    // epcis-event-validated_failure and update the CaptureStatusMessage with INVALID info
    // Consume from Topic: epcis-event-validated_failure and set CaptureStatus as Invalid and
    // produce to Topic: capture-document-event
    final KStream<String, EPCISValidationMessage> failedValidationStream =
        builder.stream(
            kafkaConfigurationService.topics().eventValidated() + "-failure",
            Consumed.with(Serdes.String(), epcisEventValidationMessageSerde));
    failedValidationStream
        .map(
            (k, v) -> {
              final CaptureStatusMessage captureStatusMessage =
                  CaptureStatusMessage.invalid(1, v.getErrors(), v.getMetadata());
              captureStatusMessage.setTraceId(v.getTraceId());
              captureStatusMessage.setSpanId(v.getSpanId());
              captureStatusMessage.setDefaultGroup(v.getDefaultGroup());
              return KeyValue.pair(k, captureStatusMessage);
            })
        .to(
            kafkaConfigurationService.topics().captureDocsEvent(),
            Produced.with(Serdes.String(), captureStatusSerde));

    // If Validation unsuccessful in the above validationStream then consume from
    // failedValidationStream (fixing for PROCEED behaviour as it was always set to running: true)
    // Increase PROCESSED count by 1 for CaptureStatusMessage after Validation unsuccessful in the
    // above validationStream
    // Produce to Topic: capture-document-event Channel: capture-document-event-out
    failedValidationStream
        .map(
            (captureID, eventValidationMessage) -> {
              final CaptureStatusMessage captureStatusMessage =
                  CaptureStatusMessage.processed(1, eventValidationMessage.getMetadata());
              captureStatusMessage.setTraceId(eventValidationMessage.getTraceId());
              captureStatusMessage.setSpanId(eventValidationMessage.getSpanId());
              captureStatusMessage.setDefaultGroup(eventValidationMessage.getDefaultGroup());
              return KeyValue.pair(captureID, captureStatusMessage);
            })
        .to(
            kafkaConfigurationService.topics().captureDocsEvent(),
            Produced.with(Serdes.String(), captureStatusSerde));

    // If successfully validated in the above validationStream then consume from Topic:
    // epcis-event-validated_success and update the CaptureStatusMessage with VALID/INVALID info
    // On PERSISTENCE   Successful set CaptureStatus as Valid   and produce to Topic:
    // capture-document-event Channel: capture-document-event-out
    // On PERSISTENCE Unsuccessful set CaptureStatus as Invalid and produce to Topic:
    // capture-document-event Channel: capture-document-event-out
    KStream<String, EPCISValidationMessage> persistenceStream =
        builder.stream(
                kafkaConfigurationService.topics().eventValidated() + "-success",
                Consumed.with(Serdes.String(), epcisEventValidationMessageSerde))
            .map(
                (KeyValueMapper<
                        String, EPCISValidationMessage, KeyValue<String, EPCISValidationMessage>>)
                    (captureID, eventValidationMessage) -> {
                      try  {

                        ObjectNode singleEventNode;

                        // If event is above 4kb and stored in s3 then get event from s3 and
                        // continue processing
                        if (StringUtils.isBlank(eventValidationMessage.getObjectNodeString())) {
                          Log.debug(
                              "Getting the event with Hash >4kb from StorageService for persisting");
                          singleEventNode =
                              (ObjectNode)
                                  objectMapper.readTree(
                                      storageService.get(
                                          eventValidationMessage.getEventStorageKey()));
                        } else {
                          // If event below 4kb and stored as string in documentCaptureMessage then
                          // read and continue as before
                          Log.debug(
                              "Event with Hash <4kb from DocumentCaptureMessage objectNodeString");
                          singleEventNode =
                              (ObjectNode)
                                  objectMapper.readTree(
                                      eventValidationMessage.getObjectNodeString());
                        }

                        final Pair<ObjectNode, Map<String, Object>> eventAndContextPair =
                            EPCISEventObjectNodeUtil.extractEventAndContextNodeFromDocument(
                                singleEventNode, objectMapper);
                        final Map<String, Object> contextAsMap = eventAndContextPair.getValue();
                        final ObjectNode eventNode = eventAndContextPair.getKey();
                        final List<InvalidEPCISEventInfo> invalidEventsInfo =
                            new ArrayList<>(eventValidationMessage.getErrors());

                        // Check if there is an error declaration, delete events reactively and
                        // record exception if any
                        if (eventNode.has(Constants.ERROR_DECLARATION)
                            && eventNode.hasNonNull(Constants.EVENT_ID)) {
                          final String eventID = eventNode.get(Constants.EVENT_ID).toString();

                          epcisEventPersistenceService
                              .deleteEventsFromID(eventID)
                              .onFailure()
                              .invoke(
                                  failure -> {
                                    log.error(
                                        "Error while deleting events for the eventID: {}",
                                        eventID,
                                        failure.getMessage());
                                  })
                              .subscribe()
                              .with(
                                  success -> {
                                    // If the event is successfully deleted from the repository.
                                    if (Boolean.TRUE.equals(success)) {
                                      log.debug(
                                          "Successfully deleted event for the eventID: {}",
                                          eventID);
                                    } else {
                                      // If the event is unable to delete from the repository such
                                      // as event does not exist issue or permission.
                                      log.warn(
                                          "Failed to delete event for the eventID: {}", eventID);
                                    }
                                    // If any error occurs during the deletion
                                  },
                                  throwable -> {
                                    log.error(
                                        "Unable to delete event for the eventID: {}",
                                        eventID,
                                        throwable.getMessage());
                                  });
                        }

                        // For proceed we ignore invalid events, and just skip the event
                        if (eventValidationMessage.isProceed() && !invalidEventsInfo.isEmpty()) {
                          if (invalidEventsInfo.stream()
                              .flatMap(
                                  invalidEPCISEventInfo ->
                                      invalidEPCISEventInfo.getSequenceInEPCISDoc().stream())
                              .anyMatch(i -> i == eventValidationMessage.getEventIndex())) {
                            log.debug(
                                "Invalid event at index: {}, proceeding to next one ",
                                eventValidationMessage.getEventIndex());
                          }
                        } else {
                          Log.debug("Persisting the event to Repository");

                          // Try to persist the event if any error/exception occurs then throw them
                          // and stop execution
                          epcisEventPersistenceService
                              .persistEvent(
                                  singleEventNode,
                                  eventNode,
                                  eventValidationMessage.getCaptureID(),
                                  contextAsMap,
                                  invalidEventsInfo,
                                  eventValidationMessage.isProceed(),
                                  eventValidationMessage.getEventIndex(),
                                  eventValidationMessage.getMetadata(),
                                  false,
                                  eventValidationMessage.getDefaultGroup())
                              .onFailure()
                              .recoverWithUni(
                                  failure -> {
                                    log.info(" ❌ Failure occurred during event persistence!!! ❌ ");
                                    return Uni.createFrom()
                                        .failure(
                                            new PersistenceException(
                                                ERROR_WHILE_PERSISTING_EVENT, failure));
                                  })
                              .subscribe()
                              .with(
                                  item -> {
                                    log.debug(" ✅ Event persistence was successful!!! ✅ ");

                                    // If persistence success then write to Kafka topic (Channel:
                                    // capture-document-event-out, capture-docs-event:
                                    // capture-document-event)
                                    final CaptureStatusMessage captureStatusMessage =
                                        CaptureStatusMessage.valid(
                                            1, eventValidationMessage.getMetadata());
                                    captureStatusMessage.setTraceId(
                                        eventValidationMessage.getTraceId());
                                    captureStatusMessage.setSpanId(
                                        eventValidationMessage.getSpanId());
                                    captureStatusMessage.setDefaultGroup(
                                        eventValidationMessage.getDefaultGroup());
                                    captureStatusMessageEmitter.send(
                                        Record.of(captureID, captureStatusMessage));
                                  },
                                  failure -> {
                                    log.warn(" Event persistence was failed for captureID!!! ");

                                    // If persistence failed then write to Kafka topic (Channel:
                                    // capture-document-event-out, capture-docs-event:
                                    // capture-document-event)
                                    final CaptureStatusMessage captureStatusMessage =
                                        CaptureStatusMessage.invalid(
                                            1,
                                            invalidEventsInfo,
                                            eventValidationMessage.getMetadata());
                                    captureStatusMessage.setTraceId(
                                        eventValidationMessage.getTraceId());
                                    captureStatusMessage.setSpanId(
                                        eventValidationMessage.getSpanId());
                                    captureStatusMessage.setDefaultGroup(
                                        eventValidationMessage.getDefaultGroup());
                                    captureStatusMessageEmitter.send(
                                        Record.of(captureID, captureStatusMessage));
                                  });
                        }
                        final EPCISValidationMessage msg = new EPCISValidationMessage();
                        msg.updateFrom(eventValidationMessage, invalidEventsInfo);

                        log.debug(
                            "persistence result for {} = {} ",
                            eventValidationMessage.getCaptureID(),
                            msg);
                        return KeyValue.pair(captureID, msg);
                      } catch (Exception e) {
                        log.error(" {}", e.getMessage(), e);
                        final EPCISValidationMessage exceptionMessage =
                            new EPCISValidationMessage();
                        exceptionMessage.updateFrom(
                            eventValidationMessage,
                            List.of(
                                new InvalidEPCISEventInfo(
                                    e.getClass().getSimpleName(),
                                    e.getMessage(),
                                    500,
                                    List.of(e.getStackTrace()).toString(),
                                    List.of(eventValidationMessage.getEventIndex()))));
                        return KeyValue.pair(captureID, exceptionMessage);
                      }
                    });

    // Write to Kafka Topic: epcis-event-persisted after event has been sent to persistence to track
    // the PROCESSED event
    persistenceStream.to(
        kafkaConfigurationService.topics().eventPersisted(),
        Produced.with(Serdes.String(), epcisEventValidationMessageSerde));

    // Consume the information from Kafka Topic: epcis-event-persisted to keep track of PROCESSED
    // event
    var captureInfoStream =
        builder.stream(
            kafkaConfigurationService.topics().eventPersisted(),
            Consumed.with(Serdes.String(), epcisEventValidationMessageSerde));

    // Increase PROCESSED count by 1 for CaptureStatusMessage after sending events to
    // epcisEventPersistenceService.persistEvent
    // Produce to Topic: capture-document-event Channel: capture-document-event-out
    captureInfoStream
        .map(
            (captureID, eventValidationMessage) -> {
              final CaptureStatusMessage captureStatusMessage =
                  CaptureStatusMessage.processed(1, eventValidationMessage.getMetadata());
              captureStatusMessage.setTraceId(eventValidationMessage.getTraceId());
              captureStatusMessage.setSpanId(eventValidationMessage.getSpanId());
              captureStatusMessage.setDefaultGroup(eventValidationMessage.getDefaultGroup());
              return KeyValue.pair(captureID, captureStatusMessage);
            })
        .to(
            kafkaConfigurationService.topics().captureDocsEvent(),
            Produced.with(Serdes.String(), captureStatusSerde));

    // Read messages from kafka topics and attach the tracing information
    builder.stream(
            kafkaConfigurationService.topics().captureDocsAgg(),
            Consumed.with(Serdes.String(), captureDataSerde))
        .foreach(
            (captureID, captureJobStatus) -> {
              log.debug("aggregation: {} = {}", captureID, captureJobStatus.toString());
            });

    return builder.build();
  }
}
