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

    public CallRecording(String recordingId, Object duration, String createdAt) {
        this(recordingId, validateDuration(duration), parseCreatedAt(createdAt));
    }

    private static int validateDuration(Object duration) {
        if (!(duration instanceof Number number)) {
            throw new IllegalArgumentException("Тривалість запису має бути невід'ємною.");
        }
        if (number instanceof Double decimal && (!Double.isFinite(decimal) || decimal != Math.rint(decimal))) {
            throw new IllegalArgumentException("Тривалість запису має бути невід'ємною.");
        }
        if (number instanceof Float decimal && (!Float.isFinite(decimal) || decimal != Math.rint(decimal))) {
            throw new IllegalArgumentException("Тривалість запису має бути невід'ємною.");
        }
        long value = number.longValue();
        if (value < Integer.MIN_VALUE || value > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("Тривалість запису має бути невід'ємною.");
        }
        return (int) value;
    }

    private static LocalDateTime parseCreatedAt(String createdAt) {
        if (createdAt == null || createdAt.trim().isEmpty()) {
            throw new IllegalArgumentException("Дата й час створення є обов'язковими.");
        }
        try {
            return LocalDateTime.parse(createdAt.trim());
        } catch (RuntimeException exception) {
            throw new IllegalArgumentException("Дата й час створення мають бути коректним ISO-8601 рядком.");
        }
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
