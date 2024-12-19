package io.openepcis.repository.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.openepcis.model.epcis.modifier.OffsetDateTimeSerializer;
import io.openepcis.repository.Constants;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

import static com.fasterxml.jackson.annotation.JsonFormat.Feature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE;
import static com.fasterxml.jackson.annotation.JsonFormat.Feature.WRITE_DATE_TIMESTAMPS_AS_NANOSECONDS;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CapturedByES {
    private String captureID;
    @JsonSerialize(using = OffsetDateTimeSerializer.class)
    @JsonFormat(without = {ADJUST_DATES_TO_CONTEXT_TIME_ZONE, WRITE_DATE_TIMESTAMPS_AS_NANOSECONDS})
    private OffsetDateTime timestamp;
    private String userID;
    private String defaultGroup;

    public static CapturedByES fromMetadata(Map<String, Object> metadata, String captureID, OffsetDateTime recordTime) {
        final Map<String, Object> capturedBy = (Map<String, Object>) metadata.get(Constants.CAPTURED_BY);
        return CapturedByES.builder()
                .captureID(captureID)
                .timestamp(recordTime)
                .userID((String) capturedBy.get(Constants.USER_ID))
                .defaultGroup((String) capturedBy.get(Constants.DEFAULT_GROUP))
                .build();
    }
}
