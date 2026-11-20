package callcenter;

import org.junit.jupiter.api.Test;
import java.lang.reflect.Field;
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

    @Test
    void missingUpdateAndDeleteWinOverForbiddenState() {
        List<CallRecording> recordings = new ArrayList<>();
        Client client = new Client("C", "Клієнт", "test@example.invalid", "000");
        Operator operator = new Operator("O", "Оператор", "Доступний");
        Call call = new Call("CALL", client, operator,
                new TelephonyService("Телефонія"), memoryStore(recordings));
        call.rejectRequest();

        assertEquals(CallCenterOperationResult.Type.NOT_FOUND,
                call.updateRecording("UNKNOWN", new CallRecording("UNKNOWN", 11,
                        LocalDateTime.now())).getType());
        assertEquals(CallCenterOperationResult.Type.NOT_FOUND,
                call.updateRecording("UNKNOWN", "DIFFERENT", 11,
                        "2026-11-20T10:00:00").getType());
        assertEquals(CallCenterOperationResult.Type.NOT_FOUND,
                call.deleteRecording("UNKNOWN").getType());
    }

    @Test
    void rejectsMutationsForRejectedStateAndUnavailableOperator() {
        List<CallRecording> recordings = new ArrayList<>();
        recordings.add(new CallRecording("REC", 10, LocalDateTime.now()));
        Client client = new Client("C", "Клієнт", "test@example.invalid", "000");
        Operator available = new Operator("O", "Оператор", "Доступний");
        Call rejected = new Call("CALL", client, available,
                new TelephonyService("Телефонія"), memoryStore(recordings));
        rejected.rejectRequest();

        assertEquals(CallCenterOperationResult.Type.CONFLICT,
                rejected.addRecording(new CallRecording("NEW", 11,
                        LocalDateTime.now())).getType());
        assertEquals(CallCenterOperationResult.Type.CONFLICT,
                rejected.updateRecording("REC", new CallRecording("REC", 12,
                        LocalDateTime.now())).getType());
        assertEquals(CallCenterOperationResult.Type.CONFLICT,
                rejected.deleteRecording("REC").getType());

        Operator unavailable = new Operator("O2", "Оператор 2", "Недоступний");
        Call unavailableCall = new Call("CALL-2", client, unavailable,
                new TelephonyService("Телефонія"), memoryStore(recordings));
        assertEquals(CallCenterOperationResult.Type.CONFLICT,
                unavailableCall.addRecording(new CallRecording("NEW-2", 11,
                        LocalDateTime.now())).getType());
        assertEquals(CallCenterOperationResult.Type.CONFLICT,
                unavailableCall.updateRecording("REC", new CallRecording("REC", 12,
                        LocalDateTime.now())).getType());
        assertEquals(CallCenterOperationResult.Type.CONFLICT,
                unavailableCall.deleteRecording("REC").getType());
    }

    @Test
    void rejectsMutationsForCallFailedQualityEvaluatedAndSummarySent() throws Exception {
        for (String forbiddenState : List.of("CallFailed", "QualityEvaluated", "SummarySent")) {
            List<CallRecording> recordings = new ArrayList<>();
            recordings.add(new CallRecording("REC", 10, LocalDateTime.now()));
            Client client = new Client("C", "Клієнт", "test@example.invalid", "000");
            Operator operator = new Operator("O", "Оператор", "Доступний");
            Call call = new Call("CALL-" + forbiddenState, client, operator,
                    new TelephonyService("Телефонія"), memoryStore(recordings));

            Field stateField = Call.class.getDeclaredField("state");
            stateField.setAccessible(true);
            stateField.set(call, forbiddenState);

            assertEquals(CallCenterOperationResult.Type.CONFLICT,
                    call.addRecording(new CallRecording("NEW", 11, LocalDateTime.now())).getType());
            assertEquals(CallCenterOperationResult.Type.CONFLICT,
                    call.updateRecording("REC", new CallRecording("REC", 12,
                            LocalDateTime.now())).getType());
            assertEquals(CallCenterOperationResult.Type.CONFLICT,
                    call.deleteRecording("REC").getType());
            assertEquals(1, recordings.size());
            assertEquals("REC", recordings.get(0).getRecordingId());
            assertEquals(10, recordings.get(0).getDuration());
        }
    }
}
