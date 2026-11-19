package callcenter;

import com.google.inject.Inject;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class SqliteRecordingStore implements RecordingStore {
    private final Connection connection;

    @Inject
    public SqliteRecordingStore(Connection connection) {
        this.connection = connection;
        ensureSchema();
    }

    private void ensureSchema() {
        String sql = "CREATE TABLE IF NOT EXISTS call_recordings "
                + "(recordingId TEXT PRIMARY KEY, duration INTEGER NOT NULL, createdAt TEXT NOT NULL)";
        try (Statement statement = connection.createStatement()) {
            statement.execute(sql);
        } catch (SQLException exception) {
            throw new IllegalStateException("Помилка підключення до бази даних SQLite: "
                    + exception.getMessage(), exception);
        }
    }

    @Override
    public void save(CallRecording recording) {
        if (recording == null) {
            throw new IllegalArgumentException("Запис розмови не може бути відсутнім.");
        }
        String sql = "INSERT INTO call_recordings (recordingId, duration, createdAt) VALUES (?, ?, ?) "
                + "ON CONFLICT(recordingId) DO UPDATE SET duration = excluded.duration, createdAt = excluded.createdAt";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, recording.getRecordingId());
            statement.setInt(2, recording.getDuration());
            statement.setString(3, recording.getCreatedAt().toString());
            statement.executeUpdate();
        } catch (SQLException exception) {
            throw new IllegalStateException("Помилка збереження даних у SQLite: "
                    + exception.getMessage(), exception);
        }
    }

    @Override
    public List<CallRecording> findAll() {
        String sql = "SELECT recordingId, duration, createdAt FROM call_recordings ORDER BY recordingId";
        List<CallRecording> recordings = new ArrayList<>();
        try (Statement statement = connection.createStatement();
             ResultSet result = statement.executeQuery(sql)) {
            while (result.next()) {
                recordings.add(new CallRecording(
                        result.getString("recordingId"),
                        result.getInt("duration"),
                        LocalDateTime.parse(result.getString("createdAt"))));
            }
            return recordings;
        } catch (SQLException | RuntimeException exception) {
            throw new IllegalStateException("Помилка читання даних із SQLite: "
                    + exception.getMessage(), exception);
        }
    }
}
