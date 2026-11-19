package callcenter;

import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CallRecordingTest {
    @Test
    void rejectsNegativeDurationAndMissingAttributes() {
        assertThrows(IllegalArgumentException.class,
                () -> new CallRecording("REC-1", -1, LocalDateTime.now()));
        assertThrows(IllegalArgumentException.class,
                () -> new CallRecording("", 1, LocalDateTime.now()));
        assertThrows(IllegalArgumentException.class,
                () -> new CallRecording("REC-1", 1, null));
    }
}
