package io.openepcis.repository.model;

import io.openepcis.model.dto.CaptureJob;
import io.openepcis.model.dto.CaptureJobStatusMessage;
import io.openepcis.model.dto.InvalidEPCISEventInfo;
import io.openepcis.repository.Constants;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CaptureJobES {
    private String captureID;
    private boolean running;
    private boolean success;
    private OffsetDateTime createdAt;
    private String captureErrorBehaviour;
    private OffsetDateTime finishedAt;
    protected List<InvalidEPCISEventInfo> errors = new ArrayList<>();
    private int validEventCount = 0;
    private int invalidEventCount = 0;
    private int processedEventCount = 0;
    private int capturedEventCount = 0;
    protected String s3Bucket;
    protected String s3Key;
    private MetadataES metadata;

    public CaptureJobES(CaptureJob captureJob, Map<String, Object> metadata) {
        this.captureID = captureJob.getCaptureID();
        this.running = captureJob.isRunning();
        this.success = captureJob.isSuccess();
        this.createdAt = captureJob.getCreatedAt();
        this.captureErrorBehaviour = captureJob.getCaptureErrorBehaviour();
        this.finishedAt = captureJob.getFinishedAt();
        this.errors = captureJob.getErrors();
        if (captureJob instanceof CaptureJobStatusMessage statusMessage) {
            this.validEventCount = statusMessage.getValidEventCount();
            this.invalidEventCount = statusMessage.getInvalidEventCount();
            this.processedEventCount = statusMessage.getProcessedEventCount();
            this.capturedEventCount = statusMessage.getCapturedEventCount();
            this.s3Bucket = statusMessage.getS3Bucket();
            this.s3Key = statusMessage.getS3Key();
        }

        CapturedByES capturedByES = getCapturedByES(captureJob, metadata);

        this.setMetadata(MetadataES.builder()
                .capturedBy(List.of(CapturedByES.builder()
                        .captureID(captureJob.getCaptureID())
                        .timestamp(captureJob.getCreatedAt())
                        .userID(capturedByES.getUserID())
                        .defaultGroup(capturedByES.getDefaultGroup())
                        .build()))
                .build());
    }

    private static CapturedByES getCapturedByES(CaptureJob captureJob, Map<String, Object> metadata) {
        return metadata.containsKey(Constants.CAPTURED_BY) && metadata.get(Constants.CAPTURED_BY) instanceof CapturedByES
                ? (CapturedByES) metadata.get(Constants.CAPTURED_BY)
                : CapturedByES.fromMetadata(metadata, captureJob.getCaptureID(), captureJob.getCreatedAt());
    }
}
