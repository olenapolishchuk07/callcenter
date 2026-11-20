package callcenter;

import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import static org.junit.jupiter.api.Assertions.assertEquals;

class CallCenterControllerTest {
    @Test
    void createsAndRejectsInvalidRecording() {
        RecordingStore store = new RecordingStore() {
            public java.util.List<CallRecording> findAll() { return new ArrayList<>(); }
            public CallRecording findById(String id) { return null; }
            public void insert(CallRecording recording) { }
            public void update(CallRecording recording) { }
            public boolean delete(String id) { return false; }
            public void save(CallRecording recording) { insert(recording); }
        };
        Call call = new Call("CALL",
                new Client("C", "Клієнт", "test@example.invalid", "000"),
                new Operator("O", "Оператор", "Доступний"),
                new TelephonyService("Телефонія"), store);
        CallCenterController controller = new CallCenterController(call);

        assertEquals(CallCenterOperationResult.Type.SUCCESS,
                controller.create("REC", 42, "2026-11-20T14:30:00").getType());
        assertEquals(CallCenterOperationResult.Type.INVALID,
                controller.create("", 42, "2026-11-20T14:30:00").getType());
    }
}
