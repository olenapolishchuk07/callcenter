package callcenter;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;

public class CallCenterController {
    private final Call call;

    public CallCenterController(Call call) {
        if (call == null) {
            throw new IllegalArgumentException("Модель дзвінка є обов'язковою.");
        }
        this.call = call;
    }

    public CallCenterOperationResult readAll() {
        return call.findAllRecordings();
    }

    public CallCenterOperationResult create(String recordingId, Integer duration, String createdAt) {
        return executeCreateOrUpdate(null, recordingId, duration, createdAt, true);
    }

    public CallCenterOperationResult update(String pathId, String recordingId, Integer duration, String createdAt) {
        return executeCreateOrUpdate(pathId, recordingId, duration, createdAt, false);
    }

    public CallCenterOperationResult delete(String recordingId) {
        return call.deleteRecording(recordingId);
    }

    private CallCenterOperationResult executeCreateOrUpdate(
            String pathId, String recordingId, Integer duration, String createdAt, boolean create) {
        if (recordingId == null || recordingId.trim().isEmpty()) {
            return CallCenterOperationResult.failure(
                    CallCenterOperationResult.Type.INVALID, "Ідентифікатор запису є обов'язковим.");
        }
        if (duration == null || duration < 0) {
            return CallCenterOperationResult.failure(
                    CallCenterOperationResult.Type.INVALID, "Тривалість запису має бути невід'ємною.");
        }
        if (createdAt == null || createdAt.trim().isEmpty()) {
            return CallCenterOperationResult.failure(
                    CallCenterOperationResult.Type.INVALID, "Дата й час створення є обов'язковими.");
        }
        try {
            CallRecording recording = new CallRecording(
                    recordingId.trim(), duration, LocalDateTime.parse(createdAt.trim()));
            if (create) {
                return call.addRecording(recording);
            }
            return call.updateRecording(pathId, recording);
        } catch (DateTimeParseException | IllegalArgumentException exception) {
            return CallCenterOperationResult.failure(
                    CallCenterOperationResult.Type.INVALID,
                    "Дата й час створення мають бути коректним ISO-8601 рядком.");
        }
    }
}
