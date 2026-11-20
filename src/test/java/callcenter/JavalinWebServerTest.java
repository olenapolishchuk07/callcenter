package callcenter;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JavalinWebServerTest {
    private JavalinWebServer server;

    @AfterEach
    void stopServer() {
        if (server != null) {
            server.stop();
        }
    }

    @Test
    void servesGetContract() throws Exception {
        List<CallRecording> recordings = new ArrayList<>();
        RecordingStore store = new RecordingStore() {
            public List<CallRecording> findAll() { return new ArrayList<>(recordings); }
            public CallRecording findById(String id) { return null; }
            public void insert(CallRecording recording) { recordings.add(recording); }
            public void update(CallRecording recording) { }
            public boolean delete(String id) { return false; }
            public void save(CallRecording recording) { insert(recording); }
        };
        Call call = new Call("CALL",
                new Client("C", "Клієнт", "test@example.invalid", "000"),
                new Operator("O", "Оператор", "Доступний"),
                new TelephonyService("Телефонія"), store);
        server = new JavalinWebServer();
        server.start(new CallCenterController(call), new CallCenterWebView(), 18080);

        HttpResponse<String> response = HttpClient.newHttpClient().send(
                HttpRequest.newBuilder(URI.create("http://localhost:18080/api/CallCenter")).GET().build(),
                HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());
        assertEquals("[]", response.body());
    }

    @Test
    void servesMutationAndErrorContracts() throws Exception {
        List<CallRecording> recordings = new ArrayList<>();
        RecordingStore store = new RecordingStore() {
            public List<CallRecording> findAll() { return new ArrayList<>(recordings); }
            public CallRecording findById(String id) {
                return recordings.stream().filter(item -> item.getRecordingId().equals(id)).findFirst().orElse(null);
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
            public void save(CallRecording recording) { insert(recording); }
        };
        Call call = new Call("CALL",
                new Client("C", "Клієнт", "test@example.invalid", "000"),
                new Operator("O", "Оператор", "Доступний"),
                new TelephonyService("Телефонія"), store);
        server = new JavalinWebServer();
        server.start(new CallCenterController(call), new CallCenterWebView(), 18081);

        HttpResponse<String> created = send("POST", "/api/CallCenter",
                "{\"recordingId\":\"REC-1\",\"duration\":10,\"createdAt\":\"2026-11-20T10:00:00\"}");
        assertEquals(201, created.statusCode());
        assertTrue(created.body().contains("успішно"));

        HttpResponse<String> duplicate = send("POST", "/api/CallCenter",
                "{\"recordingId\":\"REC-1\",\"duration\":11,\"createdAt\":\"2026-11-20T11:00:00\"}");
        assertEquals(409, duplicate.statusCode());

        HttpResponse<String> updated = send("PUT", "/api/CallCenter/REC-1",
                "{\"recordingId\":\"REC-1\",\"duration\":20,\"createdAt\":\"2026-11-20T12:00:00\"}");
        assertEquals(200, updated.statusCode());

        HttpResponse<String> missing = send("PUT", "/api/CallCenter/UNKNOWN",
                "{\"recordingId\":\"UNKNOWN\",\"duration\":20,\"createdAt\":\"2026-11-20T12:00:00\"}");
        assertEquals(404, missing.statusCode());

        HttpResponse<String> invalid = send("POST", "/api/CallCenter",
                "{\"recordingId\":\"\",\"duration\":-1,\"createdAt\":\"bad\"}");
        assertEquals(400, invalid.statusCode());

        HttpResponse<String> emptyDuration = send("POST", "/api/CallCenter",
                "{\"recordingId\":\"REC-EMPTY\",\"duration\":null,\"createdAt\":\"2026-11-20T10:00:00\"}");
        assertEquals(400, emptyDuration.statusCode());

        HttpResponse<String> malformed = send("POST", "/api/CallCenter", "{");
        assertEquals(400, malformed.statusCode());

        HttpResponse<String> deleted = send("DELETE", "/api/CallCenter/REC-1", null);
        assertEquals(200, deleted.statusCode());
        HttpResponse<String> missingDelete = send("DELETE", "/api/CallCenter/UNKNOWN", null);
        assertEquals(404, missingDelete.statusCode());
    }

    @Test
    void servesStorageErrorContract() throws Exception {
        RecordingStore store = new RecordingStore() {
            public List<CallRecording> findAll() { throw new IllegalStateException("SQLite unavailable"); }
            public CallRecording findById(String id) { throw new IllegalStateException("SQLite unavailable"); }
            public void insert(CallRecording recording) { throw new IllegalStateException("SQLite unavailable"); }
            public void update(CallRecording recording) { throw new IllegalStateException("SQLite unavailable"); }
            public boolean delete(String id) { throw new IllegalStateException("SQLite unavailable"); }
            public void save(CallRecording recording) { throw new IllegalStateException("SQLite unavailable"); }
        };
        Call call = new Call("CALL",
                new Client("C", "Клієнт", "test@example.invalid", "000"),
                new Operator("O", "Оператор", "Доступний"),
                new TelephonyService("Телефонія"), store);
        server = new JavalinWebServer();
        server.start(new CallCenterController(call), new CallCenterWebView(), 18082);

        HttpResponse<String> response = HttpClient.newHttpClient().send(
                HttpRequest.newBuilder(URI.create("http://localhost:18082/api/CallCenter")).GET().build(),
                HttpResponse.BodyHandlers.ofString());
        assertEquals(500, response.statusCode());
    }

    private HttpResponse<String> send(String method, String path, String body) throws Exception {
        HttpRequest.Builder builder = HttpRequest.newBuilder(URI.create("http://localhost:18081" + path));
        HttpRequest request;
        if (body == null) {
            request = switch (method) {
                case "DELETE" -> builder.DELETE().build();
                default -> builder.GET().build();
            };
        } else {
            request = builder.header("Content-Type", "application/json").method(method,
                    HttpRequest.BodyPublishers.ofString(body)).build();
        }
        return HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
    }
}
