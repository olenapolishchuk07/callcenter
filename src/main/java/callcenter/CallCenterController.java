package callcenter;

import com.google.inject.Inject;

public class CallCenterController {
    private final Call call;

    @Inject
    public CallCenterController(Call call) {
        if (call == null) {
            throw new IllegalArgumentException("Модель дзвінка є обов'язковою.");
        }
        this.call = call;
    }

    public CallCenterOperationResult readAll() {
        return call.findAllRecordings();
    }

    public CallCenterOperationResult create(String recordingId, Object duration, String createdAt) {
        try {
            CallRecording recording = new CallRecording(recordingId, duration, createdAt);
            return call.addRecording(recording);
        } catch (IllegalArgumentException exception) {
            return CallCenterOperationResult.failure(
                    CallCenterOperationResult.Type.INVALID, exception.getMessage());
        }
    }

    public CallCenterOperationResult update(String pathId, String recordingId, Object duration, String createdAt) {
        try {
            CallRecording recording = new CallRecording(recordingId, duration, createdAt);
            return call.updateRecording(pathId, recording);
        } catch (IllegalArgumentException exception) {
            return CallCenterOperationResult.failure(
                    CallCenterOperationResult.Type.INVALID, exception.getMessage());
        }
    }

    public CallCenterOperationResult delete(String recordingId) {
        return call.deleteRecording(recordingId);
    }

}
