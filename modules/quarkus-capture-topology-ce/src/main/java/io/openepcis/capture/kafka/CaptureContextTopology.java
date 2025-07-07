/*
 * Copyright 2022-2025 benelog GmbH & Co. KG
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

import com.fasterxml.jackson.databind.JsonNode;
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
import java.io.IOException;
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
 * CaptureContextTopology builds and manages the Kafka Streams topology that processes EPCIS
 * event‑capture messages. It integrates validation, persistence, storage, and Kafka configuration
 * to perform the following tasks:
 * <ul>
 *   <li>Consume EPCIS event‑capture messages from configured Kafka topics.</li>
 *   <li>Aggregate and update capture‑job status messages.</li>
 *   <li>Perform event validation (schema, duplication, integrity).</li>
 *   <li>Handle external storage for large EPCIS events (>4 KB).</li>
 *   <li>Produce validation success/failure messages.</li>
 *   <li>Persist capture‑job statuses in the repository.</li>
 * </ul>
 */
@RequiredArgsConstructor
@ApplicationScoped
public class CaptureContextTopology {
  private static final OpenEPCISLogger log = OpenEPCISLogger.getLogger(CaptureContextTopology.class);
  private static final int LARGE_EVENT_THRESHOLD_KB = 4;

  private final EPCISEventValidationService epcisEventValidationService;
  private final EPCISEventPersistenceService epcisEventPersistenceService;
  private final EventRepository reactiveRepository;
  private final KafkaConfigurationService kafkaConfigurationService;
  private final EventHashGenerator eventHashGenerator = new EventHashGenerator();

  @Inject ObjectMapper objectMapper;
  @Inject StorageService storageService;

  // Emitters -----------------------------------------------------------------
  @Channel("capture-document-event-out")
  @OnOverflow(OnOverflow.Strategy.UNBOUNDED_BUFFER)
  Emitter<Record<String, CaptureStatusMessage>> captureStatusMessageEmitter;

  @Channel("epcis-event-validated-success-out")
  @OnOverflow(OnOverflow.Strategy.UNBOUNDED_BUFFER)
  Emitter<Record<String, EPCISValidationMessage>> eventValidatedSuccessEmitter;

  @Channel("epcis-event-validated-failure-out")
  @OnOverflow(OnOverflow.Strategy.UNBOUNDED_BUFFER)
  Emitter<Record<String, EPCISValidationMessage>> eventValidatedFailureEmitter;

  // -------------------------------------------------------------------------

  @Produces
  public Topology buildTopology() {
    final StreamsBuilder builder = new StreamsBuilder();

    initializeSerdes(builder);
    configureEventCountHandling(builder);
    configureCaptureJobAggregation(builder);
    configureValidation(builder);
    configureFailedValidationHandling(builder);
    configurePersistence(builder);
    configureProcessedCountUpdate(builder);

    return builder.build();
  }

  /* ===================================================================== */
  /* === Configuration Methods ============================================ */
  /* ===================================================================== */

  private SerdeConfiguration initializeSerdes(StreamsBuilder builder) {
    return new SerdeConfiguration(
            new ObjectMapperSerde<>(CaptureJobStatusMessage.class),
            new ObjectMapperSerde<>(CaptureStatusMessage.class),
            new ObjectMapperSerde<>(DocumentCaptureMessage.class),
            new ObjectMapperSerde<>(EPCISValidationMessage.class),
            new ObjectMapperSerde<>(EventCountMessage.class),
            Stores.persistentKeyValueStore(kafkaConfigurationService.stores().captureDocsStore())
    );
  }

  private void configureEventCountHandling(StreamsBuilder builder) {
    var serdes = initializeSerdes(builder);

    var eventCountStream = builder.stream(
            kafkaConfigurationService.topics().captureDocEventCount(),
            Consumed.with(Serdes.String(), serdes.eventCountSerde()));

    eventCountStream
            .map(this::mapEventCountToCaptureStatus)
            .to(kafkaConfigurationService.topics().captureDocsEvent(),
                    Produced.with(Serdes.String(), serdes.captureStatusSerde()));
  }

  private void configureCaptureJobAggregation(StreamsBuilder builder) {
    var serdes = initializeSerdes(builder);

    final GlobalKTable<String, CaptureJobStatusMessage> docs = builder.globalTable(
            kafkaConfigurationService.topics().captureDocs(),
            Consumed.with(Serdes.String(), serdes.captureDataSerde()),
            Materialized.as(kafkaConfigurationService.stores().globalCaptureJobMessageStore()));

    final KStream<String, CaptureStatusMessage> eventStatusStream = builder.stream(
            kafkaConfigurationService.topics().captureDocsEvent(),
            Consumed.with(Serdes.String(), serdes.captureStatusSerde()));

    eventStatusStream.foreach((captureID, msg) ->
            log.debug("event count {} = {}", captureID, msg));

    final KStream<String, CaptureJobStatusMessage> joinedEventStatusStream = eventStatusStream
            .join(docs, (captureID, captureStatus) -> captureID, this::createAggregation)
            .groupByKey()
            .aggregate(CaptureJobStatusMessage::new, this::aggregateJobStatus,
                    Materialized.<String, CaptureJobStatusMessage>as(serdes.storeSupplier())
                            .withKeySerde(Serdes.String())
                            .withValueSerde(serdes.captureDataSerde()))
            .toStream();

    joinedEventStatusStream
            .filter((captureID, status) -> status.getFinishedAt() != null)
            .foreach(this::handleFinishedCaptureJob);

    joinedEventStatusStream.to(kafkaConfigurationService.topics().captureDocs(),
            Produced.with(Serdes.String(), serdes.captureDataSerde()));
  }

  private void configureValidation(StreamsBuilder builder) {
    var serdes = initializeSerdes(builder);

    builder.stream(kafkaConfigurationService.topics().epcisEventCaptured(),
                    Consumed.with(Serdes.String(), serdes.documentCapturedMessageSerde()))
            .map(this::processValidationMessage);
  }

  private void configureFailedValidationHandling(StreamsBuilder builder) {
    var serdes = initializeSerdes(builder);

    final KStream<String, EPCISValidationMessage> failedValidationStream = builder.stream(
            kafkaConfigurationService.topics().eventValidated() + "-failure",
            Consumed.with(Serdes.String(), serdes.epcisEventValidationMessageSerde()));

    failedValidationStream
            .map(this::toInvalidStatus)
            .to(kafkaConfigurationService.topics().captureDocsEvent(),
                    Produced.with(Serdes.String(), serdes.captureStatusSerde()));

    failedValidationStream
            .map(this::toProcessedStatus)
            .to(kafkaConfigurationService.topics().captureDocsEvent(),
                    Produced.with(Serdes.String(), serdes.captureStatusSerde()));
  }

  private void configurePersistence(StreamsBuilder builder) {
    var serdes = initializeSerdes(builder);

    KStream<String, EPCISValidationMessage> persistenceStream = builder.stream(
                    kafkaConfigurationService.topics().eventValidated() + "-success",
                    Consumed.with(Serdes.String(), serdes.epcisEventValidationMessageSerde()))
            .map(this::processPersistenceMessage);

    persistenceStream.to(kafkaConfigurationService.topics().eventPersisted(),
            Produced.with(Serdes.String(), serdes.epcisEventValidationMessageSerde()));
  }

  private void configureProcessedCountUpdate(StreamsBuilder builder) {
    var serdes = initializeSerdes(builder);

    var captureInfoStream = builder.stream(
            kafkaConfigurationService.topics().eventPersisted(),
            Consumed.with(Serdes.String(), serdes.epcisEventValidationMessageSerde()));

    captureInfoStream
            .map(this::toProcessedStatus)
            .to(kafkaConfigurationService.topics().captureDocsEvent(),
                    Produced.with(Serdes.String(), serdes.captureStatusSerde()));

    builder.stream(kafkaConfigurationService.topics().captureDocsAgg(),
                    Consumed.with(Serdes.String(), serdes.captureDataSerde()))
            .foreach((captureID, status) -> log.debug("aggregation: {} = {}", captureID, status));
  }

  /* ===================================================================== */
  /* === Helper Methods ==================================================== */
  /* ===================================================================== */

  /**
   * Safely parses JSON input stream to ObjectNode with proper error handling.
   */
  private static ObjectNode parseObjectNode(ObjectMapper mapper, InputStream is) throws IOException {
    JsonNode node = mapper.readTree(is);
    if (node instanceof ObjectNode objectNode) {
      return objectNode;
    }
    throw new IllegalArgumentException("Expected JSON ObjectNode but got " + node.getNodeType());
  }

  /**
   * Safely parses JSON string to ObjectNode with proper error handling.
   */
  private static ObjectNode parseObjectNode(ObjectMapper mapper, String json) throws IOException {
    JsonNode node = mapper.readTree(json);
    if (node instanceof ObjectNode objectNode) {
      return objectNode;
    }
    throw new IllegalArgumentException("Expected JSON ObjectNode but got " + node.getNodeType());
  }

  /**
   * Reads event data from storage or string based on size.
   */
  private EventData readEventData(DocumentCaptureMessage docMsg) throws IOException {
    if (StringUtils.isBlank(docMsg.getObjectNodeString())) {
      log.debug(String.format("Reading large event (>%d kb) from StorageService %s",
              LARGE_EVENT_THRESHOLD_KB, docMsg.getStorageKey()));

      try (InputStream originalStream = storageService.get(docMsg.getEventStorageKey())) {
        byte[] content = originalStream.readAllBytes();

        // Generate hash first
        String[] hashes = eventHashGenerator
                .fromJson(new ByteArrayInputStream(content), "sha-256")
                .subscribe().asStream().toList().toArray(new String[0]);

        // Parse JSON
        ObjectNode eventNode = parseObjectNode(objectMapper, new ByteArrayInputStream(content));

        return new EventData(eventNode, hashes, true);
      }
    } else {
      Log.debug(String.format("Reading small event (<%d kb) from DocumentCaptureMessage", LARGE_EVENT_THRESHOLD_KB));

      String eventString = docMsg.getObjectNodeString();
      ObjectNode eventNode = parseObjectNode(objectMapper, eventString);

      String[] hashes = eventHashGenerator
              .fromJson(new ByteArrayInputStream(eventString.getBytes(StandardCharsets.UTF_8)), "sha-256")
              .subscribe().asStream().toList().toArray(new String[0]);

      return new EventData(eventNode, hashes, false);
    }
  }

  private KeyValue<String, CaptureStatusMessage> mapEventCountToCaptureStatus(String captureID, EventCountMessage eventCount) {
    final CaptureStatusMessage captureStatusMessage = CaptureStatusMessage.eventCapturedCount(
            eventCount.getCount(), new HashMap<>());
    captureStatusMessage.setTraceId(eventCount.getTraceId());
    captureStatusMessage.setSpanId(eventCount.getSpanId());
    captureStatusMessage.setDefaultGroup(eventCount.getDefaultGroup());
    return KeyValue.pair(captureID, captureStatusMessage);
  }

  private CaptureJobStatusMessageAggregation createAggregation(String captureID,
                                                               CaptureStatusMessage captureStatus, CaptureJobStatusMessage captureJobStatus) {
    return new CaptureJobStatusMessageAggregation(
            captureJobStatus, captureStatus, epcisEventPersistenceService);
  }

  private CaptureJobStatusMessage aggregateJobStatus(String captureID,
                                                     CaptureJobStatusMessageAggregation aggregation, CaptureJobStatusMessage jobStatus) {
    return aggregation
            .update(jobStatus)
            .chain(msg -> {
              log.debug("aggregating from {}", msg);
              msg.setErrors(InvalidEventInfoUtil.condenseInvalidInfos(msg.getErrors()));
              return Uni.createFrom().item(msg);
            })
            .subscribe()
            .asCompletionStage()
            .join();
  }

  private KeyValue<String, EPCISValidationMessage> processValidationMessage(String captureID,
                                                                            DocumentCaptureMessage documentCaptureMessage) {
    try {
      EventData eventData = readEventData(documentCaptureMessage);

      // Extract event and context
      final Pair<ObjectNode, Map<String, Object>> eventAndContextPair =
              EPCISEventObjectNodeUtil.extractEventAndContextNodeFromDocument(
                      eventData.eventNode(), objectMapper);
      final Map<String, Object> contextAsMap = eventAndContextPair.getValue();
      final ObjectNode eventNode = eventAndContextPair.getKey();

      final List<InvalidEPCISEventInfo> invalidEPCISEvents = new ArrayList<>();

      epcisEventValidationService
              .validateEvent(
                      eventData.eventNode(),
                      eventNode,
                      eventData.hashes(),
                      contextAsMap,
                      documentCaptureMessage.getCaptureID(),
                      invalidEPCISEvents,
                      documentCaptureMessage.getEventIndex(),
                      documentCaptureMessage.getEventIDs(),
                      eventNode.has(Constants.ERROR_DECLARATION),
                      documentCaptureMessage.getMetadata())
              .subscribe()
              .with(
                      item -> handleValidationSuccess(captureID, documentCaptureMessage,
                              eventData.eventNode(), invalidEPCISEvents, eventData.isLargeEvent()),
                      failure -> handleValidationFailure(captureID, documentCaptureMessage,
                              invalidEPCISEvents, failure));

      final EPCISValidationMessage msg = new EPCISValidationMessage();
      msg.updateFrom(documentCaptureMessage, invalidEPCISEvents);
      log.info("validation result for {} = {}", documentCaptureMessage.getCaptureID(), msg);
      return KeyValue.pair(captureID, msg);

    } catch (Exception e) {
      log.error("Exception during validation/hash generation: {}", e.getMessage(), e);
      return handleValidationException(captureID, documentCaptureMessage, e);
    }
  }

  private KeyValue<String, EPCISValidationMessage> processPersistenceMessage(String captureID,
                                                                             EPCISValidationMessage eventValidationMessage) {
    try {
      ObjectNode singleEventNode = readEventForPersistence(eventValidationMessage);

      final Pair<ObjectNode, Map<String, Object>> eventAndContextPair =
              EPCISEventObjectNodeUtil.extractEventAndContextNodeFromDocument(singleEventNode, objectMapper);
      final Map<String, Object> contextAsMap = eventAndContextPair.getValue();
      final ObjectNode eventNode = eventAndContextPair.getKey();
      final List<InvalidEPCISEventInfo> invalidEventsInfo = new ArrayList<>(eventValidationMessage.getErrors());

      if (eventNode.has(Constants.ERROR_DECLARATION) && eventNode.hasNonNull(Constants.EVENT_ID)) {
        handleErrorDeclaration(eventNode);
      }

      if (shouldSkipInvalidEvent(eventValidationMessage, invalidEventsInfo)) {
        log.debug("Invalid event at index: {}, proceeding", eventValidationMessage.getEventIndex());
      } else {
        persistEvent(captureID, eventValidationMessage, singleEventNode, eventNode, contextAsMap, invalidEventsInfo);
      }

      final EPCISValidationMessage msg = new EPCISValidationMessage();
      msg.updateFrom(eventValidationMessage, invalidEventsInfo);
      log.debug("persistence result for {} = {}", eventValidationMessage.getCaptureID(), msg);
      return KeyValue.pair(captureID, msg);

    } catch (Exception e) {
      log.error("Exception during persistence: {}", e.getMessage(), e);
      return handlePersistenceException(captureID, eventValidationMessage, e);
    }
  }

  private ObjectNode readEventForPersistence(EPCISValidationMessage validationMessage) throws IOException {
    if (StringUtils.isBlank(validationMessage.getObjectNodeString())) {
      Log.debug(String.format("Getting large event (>%d kb) from StorageService for persistence", LARGE_EVENT_THRESHOLD_KB));
      try (InputStream stream = storageService.get(validationMessage.getEventStorageKey())) {
        byte[] content = stream.readAllBytes();
        if (content.length == 0) {
          throw new IOException("Storage content is empty for key: " + validationMessage.getEventStorageKey());
        }
        return parseObjectNode(objectMapper, new ByteArrayInputStream(content));
      }
    } else {
      Log.debug(String.format("Reading small event (<%d kb) from DocumentCaptureMessage", LARGE_EVENT_THRESHOLD_KB));
      String jsonString = validationMessage.getObjectNodeString();
      if (StringUtils.isBlank(jsonString)) {
        throw new IOException("ObjectNodeString is empty for small event");
      }
      return parseObjectNode(objectMapper, jsonString);
    }
  }

  private boolean shouldSkipInvalidEvent(EPCISValidationMessage validationMessage,
                                         List<InvalidEPCISEventInfo> invalidEventsInfo) {
    return validationMessage.isProceed() && !invalidEventsInfo.isEmpty() &&
            invalidEventsInfo.stream()
                    .flatMap(i -> i.getSequenceInEPCISDoc().stream())
                    .anyMatch(i -> i == validationMessage.getEventIndex());
  }

  private void handleValidationSuccess(String captureID, DocumentCaptureMessage docMsg,
                                       ObjectNode singleEventNode, List<InvalidEPCISEventInfo> invalidEPCISEvents, boolean isLargeEvent) {
    final String eventWithHash = singleEventNode.toString();
    final Runnable emitValidationResult = () -> {
      EPCISValidationMessage vm = new EPCISValidationMessage();
      vm.updateFrom(docMsg, invalidEPCISEvents);

      if (invalidEPCISEvents.isEmpty()) {
        Log.debug("✅ Event validation successful");
        eventValidatedSuccessEmitter.send(Record.of(captureID, vm));
      } else {
        Log.debug("❌ Event validation failed");
        eventValidatedFailureEmitter.send(Record.of(captureID, vm));
      }
    };

    if (isLargeEvent) {
      updateLargeEventInStorage(docMsg, eventWithHash, emitValidationResult);
    } else {
      docMsg.setObjectNodeString(eventWithHash);
      emitValidationResult.run();
    }
  }

  private void updateLargeEventInStorage(DocumentCaptureMessage docMsg, String eventWithHash,
                                         Runnable callback) {
    byte[] bytes = eventWithHash.getBytes(StandardCharsets.UTF_8);
    InputStream stream = new ByteArrayInputStream(bytes);
    Map<String, String> tags = docMsg.getEventStorageTags();

    storageService.put(
                    docMsg.getEventStorageKey(),
                    tags.get("Content_Type"),
                    Optional.of((long) bytes.length),
                    tags,
                    stream)
            .replaceWithVoid()
            .subscribe()
            .with(
                    success -> {
                      Log.debug(String.format("Successfully updated event with hash for key: %s", docMsg.getEventStorageKey()));
                      callback.run();
                    },
                    failure -> Log.error(String.format("Failed to update event with hash for key: %s", docMsg.getEventStorageKey()), failure));
  }

  private void handleValidationFailure(String captureID, DocumentCaptureMessage docMsg,
                                       List<InvalidEPCISEventInfo> invalids, Throwable failure) {
    log.info("❌ Event validation failed: {}", failure.getMessage());
    EPCISValidationMessage vm = new EPCISValidationMessage();
    vm.updateFrom(docMsg, invalids);
    eventValidatedFailureEmitter.send(Record.of(captureID, vm));
  }

  private KeyValue<String, EPCISValidationMessage> handleValidationException(String captureID,
                                                                             DocumentCaptureMessage docMsg, Exception e) {
    final EPCISValidationMessage exceptionMessage = new EPCISValidationMessage();
    exceptionMessage.updateFrom(docMsg, List.of(
            new InvalidEPCISEventInfo(
                    e.getClass().getSimpleName(),
                    e.getMessage(),
                    500,
                    Arrays.toString(e.getStackTrace()),
                    List.of(docMsg.getEventIndex()))));
    eventValidatedFailureEmitter.send(Record.of(captureID, exceptionMessage));
    return KeyValue.pair(captureID, exceptionMessage);
  }

  private KeyValue<String, EPCISValidationMessage> handlePersistenceException(String captureID,
                                                                              EPCISValidationMessage validationMessage, Exception e) {
    final EPCISValidationMessage exceptionMessage = new EPCISValidationMessage();
    exceptionMessage.updateFrom(validationMessage, List.of(
            new InvalidEPCISEventInfo(
                    e.getClass().getSimpleName(),
                    e.getMessage(),
                    500,
                    Arrays.toString(e.getStackTrace()),
                    List.of(validationMessage.getEventIndex()))));
    return KeyValue.pair(captureID, exceptionMessage);
  }

  private KeyValue<String, CaptureStatusMessage> toInvalidStatus(String key, EPCISValidationMessage v) {
    CaptureStatusMessage m = CaptureStatusMessage.invalid(1, v.getErrors(), v.getMetadata());
    m.setTraceId(v.getTraceId());
    m.setSpanId(v.getSpanId());
    m.setDefaultGroup(v.getDefaultGroup());
    return KeyValue.pair(key, m);
  }

  private KeyValue<String, CaptureStatusMessage> toProcessedStatus(String captureID, EPCISValidationMessage v) {
    CaptureStatusMessage m = CaptureStatusMessage.processed(1, v.getMetadata());
    m.setTraceId(v.getTraceId());
    m.setSpanId(v.getSpanId());
    m.setDefaultGroup(v.getDefaultGroup());
    return KeyValue.pair(captureID, m);
  }

  private void handleFinishedCaptureJob(String captureID, CaptureJobStatusMessage msg) {
    try {
      if (msg.getInvalidEventCount() > 0 && msg.isRollback()) {
        epcisEventPersistenceService.removeEventsForCaptureID(
                        msg.getCaptureID(), msg.getCreatedAt(), msg.getDefaultGroup())
                .subscribe()
                .with(
                        success -> log.debug("Deleted events for captureId: {}", msg.getCaptureID()),
                        throwable -> log.error("Error deleting events for captureId: {}",
                                msg.getCaptureID(), throwable));
        log.debug("EPCIS document with captureId={} rolled back due to invalid events", captureID);
      }

      if (msg.getCapturedEventCount() > msg.getProcessedEventCount()) {
        msg.setCapturedEventCount(msg.getProcessedEventCount());
        msg.setFinishedAt(OffsetDateTime.now());
        msg.setSuccess(msg.getErrors().isEmpty());
        msg.setRunning(false);
      }

      reactiveRepository.saveCaptureJob(msg, captureID, msg.getCreatedAt(),
                      msg.getDefaultGroup(), msg.getMetadata())
              .onFailure()
              .invoke(f -> log.error("Error saving capture job: {}", f.getMessage()))
              .subscribe()
              .with(
                      res -> log.debug("Saved capture job {}", captureID),
                      throwable -> log.debug("Failed to save capture job {}", captureID, throwable));
    } catch (Exception e) {
      log.error("Exception in handleFinishedCaptureJob: {}", e.getMessage(), e);
    }
  }

  private void handleErrorDeclaration(ObjectNode eventNode) {
    String eventID = eventNode.get(Constants.EVENT_ID).toString();
    epcisEventPersistenceService.deleteEventsFromID(eventID)
            .onFailure()
            .invoke(f -> log.error("Error deleting events for eventID: {}", eventID, f))
            .subscribe()
            .with(
                    success -> {
                      if (Boolean.TRUE.equals(success)) {
                        log.debug("Deleted event for eventID: {}", eventID);
                      } else {
                        log.warn("Failed to delete event for eventID: {}", eventID);
                      }
                    },
                    throwable -> log.error("Unable to delete event for eventID: {}", eventID, throwable));
  }

  private void persistEvent(String captureID, EPCISValidationMessage vMsg, ObjectNode singleEventNode,
                            ObjectNode eventNode, Map<String, Object> contextAsMap, List<InvalidEPCISEventInfo> invalidEventsInfo) {
    Log.debug("Persisting event to Repository");
    epcisEventPersistenceService.persistEvent(
                    singleEventNode, eventNode, vMsg.getCaptureID(), contextAsMap, invalidEventsInfo,
                    vMsg.isProceed(), vMsg.getEventIndex(), vMsg.getMetadata(), false, vMsg.getDefaultGroup())
            .onFailure()
            .recoverWithUni(failure -> {
              log.info("❌ Failure during event persistence");
              return Uni.createFrom().failure(new PersistenceException(ERROR_WHILE_PERSISTING_EVENT, failure));
            })
            .subscribe()
            .with(
                    item -> {
                      log.debug("✅ Event persistence successful");
                      CaptureStatusMessage m = CaptureStatusMessage.valid(1, vMsg.getMetadata());
                      setTraceInfo(m, vMsg);
                      captureStatusMessageEmitter.send(Record.of(captureID, m));
                    },
                    failure -> {
                      log.warn("Event persistence failed for captureID {}", captureID);
                      CaptureStatusMessage m = CaptureStatusMessage.invalid(1, invalidEventsInfo, vMsg.getMetadata());
                      setTraceInfo(m, vMsg);
                      captureStatusMessageEmitter.send(Record.of(captureID, m));
                    });
  }

  private void setTraceInfo(CaptureStatusMessage message, EPCISValidationMessage source) {
    message.setTraceId(source.getTraceId());
    message.setSpanId(source.getSpanId());
    message.setDefaultGroup(source.getDefaultGroup());
  }

  /* ===================================================================== */
  /* === Data Classes ===================================================== */
  /* ===================================================================== */

  private record EventData(ObjectNode eventNode, String[] hashes, boolean isLargeEvent) {}

  private record SerdeConfiguration(
          ObjectMapperSerde<CaptureJobStatusMessage> captureDataSerde,
          ObjectMapperSerde<CaptureStatusMessage> captureStatusSerde,
          ObjectMapperSerde<DocumentCaptureMessage> documentCapturedMessageSerde,
          ObjectMapperSerde<EPCISValidationMessage> epcisEventValidationMessageSerde,
          ObjectMapperSerde<EventCountMessage> eventCountSerde,
          KeyValueBytesStoreSupplier storeSupplier
  ) {}
}