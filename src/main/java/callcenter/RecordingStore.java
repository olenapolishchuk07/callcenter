package callcenter;

import java.util.List;

public interface RecordingStore {
    List<CallRecording> findAll();
    CallRecording findById(String recordingId);
    void insert(CallRecording recording);
    void update(CallRecording recording);
    boolean delete(String recordingId);
    @Deprecated
    void save(CallRecording recording);
}
