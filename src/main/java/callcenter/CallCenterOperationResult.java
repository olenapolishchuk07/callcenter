package callcenter;

import java.util.ArrayList;
import java.util.List;

public class CallCenterOperationResult {
    public enum Type {
        SUCCESS,
        INVALID,
        CONFLICT,
        NOT_FOUND,
        STORAGE_ERROR
    }

    private final boolean success;
    private final String message;
    private final Type type;
    private final List<CallRecording> recordings;
    private final CallRecording recording;

    private CallCenterOperationResult(boolean success, String message, Type type,
                                      List<CallRecording> recordings, CallRecording recording) {
        this.success = success;
        this.message = message;
        this.type = type;
        this.recordings = recordings == null ? new ArrayList<>() : new ArrayList<>(recordings);
        this.recording = recording;
    }

    public static CallCenterOperationResult success(String message) {
        return new CallCenterOperationResult(true, message, Type.SUCCESS, null, null);
    }

    public static CallCenterOperationResult success(String message, List<CallRecording> recordings) {
        return new CallCenterOperationResult(true, message, Type.SUCCESS, recordings, null);
    }

    public static CallCenterOperationResult success(String message, CallRecording recording) {
        return new CallCenterOperationResult(true, message, Type.SUCCESS, null, recording);
    }

    public static CallCenterOperationResult failure(Type type, String message) {
        return new CallCenterOperationResult(false, message, type, null, null);
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public Type getType() {
        return type;
    }

    public List<CallRecording> getRecordings() {
        return new ArrayList<>(recordings);
    }

    public CallRecording getRecording() {
        return recording;
    }
}
