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
        server.start(new CallCenterController(call), 18080);

        HttpResponse<String> response = HttpClient.newHttpClient().send(
                HttpRequest.newBuilder(URI.create("http://localhost:18080/api/CallCenter")).GET().build(),
                HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());
        assertEquals("[]", response.body());
    }
}
