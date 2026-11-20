package callcenter;

import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CallTest {
    private static RecordingStore memoryStore(List<CallRecording> recordings) {
        return new RecordingStore() {
            public List<CallRecording> findAll() {
                return new ArrayList<>(recordings);
            }
            public CallRecording findById(String id) {
                return recordings.stream()
                        .filter(item -> item.getRecordingId().equals(id))
                        .findFirst().orElse(null);
            }
            public void insert(CallRecording recording) {
                if (findById(recording.getRecordingId()) != null) {
                    throw new IllegalArgumentException("Запис із таким recordingId вже існує.");
                }
                recordings.add(recording);
            }
            public void update(CallRecording recording) {
                recordings.removeIf(item -> item.getRecordingId().equals(recording.getRecordingId()));
                recordings.add(recording);
            }
            public boolean delete(String id) {
                return recordings.removeIf(item -> item.getRecordingId().equals(id));
            }
            public void save(CallRecording recording) {
                insert(recording);
            }
        };
    }

    @Test
    void addsUpdatesAndDeletesRecordings() {
        List<CallRecording> recordings = new ArrayList<>();
        Client client = new Client("C", "Клієнт", "test@example.invalid", "000");
        Operator operator = new Operator("O", "Оператор", "Доступний");
        Call call = new Call("CALL", client, operator,
                new TelephonyService("Телефонія"), memoryStore(recordings));

        CallRecording original = new CallRecording("REC", 10,
                LocalDateTime.of(2026, 11, 20, 10, 0));
        assertTrue(call.addRecording(original).isSuccess());
        assertTrue(call.updateRecording("REC", new CallRecording("REC", 20,
                LocalDateTime.of(2026, 11, 20, 11, 0))).isSuccess());
        assertTrue(call.deleteRecording("REC").isSuccess());
        assertTrue(recordings.isEmpty());
    }

    @Test
    void rejectsDuplicateAndMissingUpdate() {
        List<CallRecording> recordings = new ArrayList<>();
        recordings.add(new CallRecording("REC", 10, LocalDateTime.now()));
        Client client = new Client("C", "Клієнт", "test@example.invalid", "000");
        Operator operator = new Operator("O", "Оператор", "Доступний");
        Call call = new Call("CALL", client, operator,
                new TelephonyService("Телефонія"), memoryStore(recordings));

        assertFalse(call.addRecording(new CallRecording("REC", 11,
                LocalDateTime.now())).isSuccess());
        assertEquals(CallCenterOperationResult.Type.NOT_FOUND,
                call.updateRecording("UNKNOWN", new CallRecording("UNKNOWN", 11,
                        LocalDateTime.now())).getType());
    }
}
