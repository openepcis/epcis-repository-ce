package io.openepcis.capture.kafka;

import io.openepcis.capture.context.message.EventSavedMessage;
import io.openepcis.capture.service.EPCISEventPersistenceService;
import io.openepcis.model.dto.CaptureJobStatusMessage;
import io.openepcis.model.dto.CaptureStatusMessage;
import io.openepcis.model.epcis.exception.PersistenceException;
import io.openepcis.opentelemetry.logging.OpenEPCISLogger;
import io.openepcis.service.util.Constants;
import io.smallrye.mutiny.Uni;
import java.time.OffsetDateTime;

// Method to set the information for the capture job based on the
public record CaptureJobStatusMessageAggregation(
    CaptureJobStatusMessage captureJob,
    CaptureStatusMessage statusMessage,
    EPCISEventPersistenceService epcisEventPersistenceService) {
  private static final OpenEPCISLogger log =
      OpenEPCISLogger.getLogger(CaptureContextTopology.class);

  public Uni<CaptureJobStatusMessage> update(CaptureJobStatusMessage job) {
    job.setCaptureID(captureJob.getCaptureID());
    job.setCreatedAt(captureJob.getCreatedAt());
    job.setCaptureErrorBehaviour(captureJob.getCaptureErrorBehaviour());
    job.setStorageBucket(captureJob.getStorageBucket());
    job.setStorageKey(captureJob.getStorageKey());
    job.setTraceId(statusMessage.getTraceId());
    job.setSpanId(statusMessage.getSpanId());
    job.setDefaultGroup(statusMessage.getDefaultGroup());
    job.setMetadata(statusMessage.getMetadata());
    if (statusMessage.getType() != null) {
      switch (statusMessage.getType()) {
        case CAPTURED -> job.setCapturedEventCount(statusMessage.getValue());
        case INVALID -> {
          job.setInvalidEventCount(job.getInvalidEventCount() + statusMessage.getValue());
          job.getErrors().addAll(statusMessage.getErrors());
          if (job.getCaptureErrorBehaviour()
              .equalsIgnoreCase(Constants.CAPTURE_ERROR_BEHAVIOUR_ROLLBACK)) {
            job.setFinishedAt(OffsetDateTime.now());
            job.setSuccess(job.getErrors().isEmpty());
            job.setRunning(false);
          }
        }
        case VALID -> job.setValidEventCount(job.getValidEventCount() + statusMessage.getValue());
        case PROCESSED -> {
          job.setProcessedEventCount(job.getProcessedEventCount() + statusMessage.getValue());
          if (job.getCapturedEventCount() == job.getProcessedEventCount()) {
            job.setFinishedAt(OffsetDateTime.now());
            job.setSuccess(job.getErrors().isEmpty());
            job.setRunning(false);
            if (job.isSuccess() && job.isRollback()) {
              try {
                return produceMessageToEventSavedTopic(job);
              } catch (Exception e) {
                log.error(
                    "ERROR while producing event to event-saved topic for events with captureId= {}",
                    job.getCaptureID(),
                    e);
                return Uni.createFrom().failure(e);
              }
            }
          }
        }
      }
    }
    return Uni.createFrom().item(job);
  }

  // Method to get the eventID for the persisted event after the process of persisting
  private Uni<CaptureJobStatusMessage> produceMessageToEventSavedTopic(
      CaptureJobStatusMessage job) {
    log.debug(
        "Total events to be produced in event saved topic with captured ID= {} are: {}",
        job.getCaptureID(),
        job.getProcessedEventCount());

    return epcisEventPersistenceService
        .checkAllEventsAvailableForGivenCaptureID(job.getCaptureID(), job.getProcessedEventCount())
        .chain(
            eventCount -> {
              log.debug(
                  "updating visible field value to true for events with captureID = {}",
                  captureJob.getCaptureID());
              return epcisEventPersistenceService.updateEventVisibility(
                  job.getCaptureID(), job.getCreatedAt(), job.getDefaultGroup());
            })
        .chain(
            success -> {
              if (success.equals(Boolean.FALSE)) {
                return Uni.createFrom()
                    .failure(
                        new PersistenceException(
                            String.format(
                                "unable to update event visibility for captureID = %s",
                                job.getCaptureID())));
              }
              return epcisEventPersistenceService
                  .getEventIdsByCaptureId(job.getCaptureID())
                  .onItem()
                  .transformToUni(
                      eventID -> {
                        log.debug("producing event to event-saved topic with eventID= {}", eventID);
                        final EventSavedMessage eventSavedMessage =
                            EventSavedMessage.builder().eventID(eventID).build();
                        eventSavedMessage.setTraceId(job.getTraceId());
                        eventSavedMessage.setSpanId(job.getSpanId());
                        eventSavedMessage.setDefaultGroup(job.getDefaultGroup());
                        log.debug("Sending message for event with eventID: {}", eventID);
                        return epcisEventPersistenceService
                            .produceMessageToEventSavedTopic(eventSavedMessage)
                            .chain(v -> Uni.createFrom().item(eventID));
                      })
                  .merge()
                  .collect()
                  .asList()
                  .chain(l -> Uni.createFrom().item(job));
            });
  }
}
