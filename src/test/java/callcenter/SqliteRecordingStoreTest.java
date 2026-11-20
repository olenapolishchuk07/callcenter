package callcenter;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.time.LocalDateTime;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SqliteRecordingStoreTest {
    private Path database;
    private Connection connection;
    private SqliteRecordingStore store;

    @BeforeEach
    void setUp() throws Exception {
        database = Files.createTempFile("callcenter", ".db");
        Files.delete(database);
        connection = DriverManager.getConnection("jdbc:sqlite:" + database);
        store = new SqliteRecordingStore(connection);
    }

    @AfterEach
    void tearDown() throws Exception {
        connection.close();
        Files.deleteIfExists(database);
    }

    @Test
    void insertAndUpdateWithoutDuplicates() {
        LocalDateTime firstTime = LocalDateTime.of(2026, 11, 19, 20, 4);
        store.insert(new CallRecording("REC-1", 120, firstTime));
        store.update(new CallRecording("REC-1", 180,
                firstTime.plusHours(1)));

        List<CallRecording> recordings = store.findAll();
        assertEquals(1, recordings.size());
        assertEquals(180, recordings.get(0).getDuration());
        assertEquals(firstTime.plusHours(1), recordings.get(0).getCreatedAt());
    }

    @Test
    void rejectsDuplicateInsertUnknownUpdateAndDeleteResults() {
        LocalDateTime time = LocalDateTime.of(2026, 11, 19, 20, 4);
        store.insert(new CallRecording("REC-1", 120, time));

        assertThrows(IllegalArgumentException.class,
                () -> store.insert(new CallRecording("REC-1", 180, time)));
        assertThrows(IllegalArgumentException.class,
                () -> store.update(new CallRecording("UNKNOWN", 180, time)));

        assertTrue(store.delete("REC-1"));
        assertFalse(store.delete("REC-1"));
        assertFalse(store.delete("UNKNOWN"));
    }

    @Test
    void readsAllAttributesAndEmptyStore() {
        assertEquals(0, store.findAll().size());
        LocalDateTime time = LocalDateTime.of(2026, 11, 19, 20, 4, 5);
        store.save(new CallRecording("REC-2", 42, time));
        CallRecording recording = store.findAll().get(0);
        assertEquals("REC-2", recording.getRecordingId());
        assertEquals(42, recording.getDuration());
        assertEquals(time, recording.getCreatedAt());
    }

    @Test
    void reportsUkrainianSqliteSaveError() throws Exception {
        connection.close();
        assertThrows(IllegalStateException.class,
                () -> store.insert(new CallRecording("REC-ERR", 1, LocalDateTime.now())),
                "Помилка збереження даних у SQLite:");
    }

    @Test
    void reportsUkrainianSqliteReadErrorAndStops() throws Exception {
        connection.close();
        IllegalStateException exception = assertThrows(IllegalStateException.class, store::findAll);
        assertEquals(true, exception.getMessage().startsWith("Помилка читання даних із SQLite:"));
    }
}
