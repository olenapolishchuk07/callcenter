package callcenter;

import java.util.List;

public interface RecordingStore {
    void save(CallRecording recording);
    List<CallRecording> findAll();
}
