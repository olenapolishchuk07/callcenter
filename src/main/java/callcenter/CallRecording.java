package callcenter;

import java.time.LocalDateTime;

public class CallRecording {
    private final String recordingId;
    private final int duration;
    private final LocalDateTime createdAt;

    public CallRecording(String recordingId, int duration, LocalDateTime createdAt) {
        if (recordingId == null || recordingId.trim().isEmpty()) {
            throw new IllegalArgumentException("Ідентифікатор запису є обов'язковим.");
        }
        if (duration < 0) {
            throw new IllegalArgumentException("Тривалість запису не може бути від'ємною.");
        }
        if (createdAt == null) {
            throw new IllegalArgumentException("Дата й час створення запису є обов'язковими.");
        }
        this.recordingId = recordingId;
        this.duration = duration;
        this.createdAt = createdAt;
    }

    public String listen() {
        String description = "запис " + recordingId + " доступний для прослуховування";
        System.out.println("Симуляція прослуховування: " + description + ".");
        return description;
    }

    public String getRecordingId() {
        return recordingId;
    }

    public int getDuration() {
        return duration;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
