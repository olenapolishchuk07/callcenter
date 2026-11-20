package callcenter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.javalin.Javalin;
import io.javalin.http.Context;
import java.util.Map;

public class JavalinWebServer implements WebServer {
    private final ObjectMapper objectMapper = new ObjectMapper();
    private Javalin app;

    public JavalinWebServer() {
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    @Override
    public void start(CallCenterController controller, CallCenterWebView view, int port) {
        app = Javalin.create(config -> config.staticFiles.add("/public"));

        app.get("/", ctx -> ctx.redirect("/index.html"));
        app.get("/api/CallCenter", ctx ->
                apply(ctx, view.read(controller.readAll())));
        app.post("/api/CallCenter", ctx -> {
            try {
                Map<String, Object> body = readRecording(ctx);
                CallCenterOperationResult result = controller.create(
                        stringValue(body, "recordingId"),
                        body.get("duration"),
                        stringValue(body, "createdAt"));
                apply(ctx, view.create(result));
            } catch (Exception exception) {
                apply(ctx, view.failure(CallCenterOperationResult.failure(
                        CallCenterOperationResult.Type.INVALID,
                        "Некоректне JSON-повідомлення.")));
            }
        });
        app.put("/api/CallCenter/{id}", ctx -> {
            try {
                Map<String, Object> body = readRecording(ctx);
                CallCenterOperationResult result = controller.update(
                        ctx.pathParam("id"),
                        stringValue(body, "recordingId"),
                        body.get("duration"),
                        stringValue(body, "createdAt"));
                apply(ctx, view.update(result));
            } catch (Exception exception) {
                apply(ctx, view.failure(CallCenterOperationResult.failure(
                        CallCenterOperationResult.Type.INVALID,
                        "Некоректне JSON-повідомлення.")));
            }
        });
        app.delete("/api/CallCenter/{id}", ctx ->
                apply(ctx, view.delete(controller.delete(ctx.pathParam("id")))));

        app.exception(Exception.class, (exception, ctx) ->
                apply(ctx, view.failure(CallCenterOperationResult.failure(
                        CallCenterOperationResult.Type.STORAGE_ERROR,
                        "Сталася помилка сервера під час обробки запиту."))));
        app.start(port);
    }

    @Override
    public void stop() {
        if (app != null) {
            app.stop();
        }
    }

    private Map<String, Object> readRecording(Context ctx) throws Exception {
        return objectMapper.readValue(ctx.body(), Map.class);
    }

    private String stringValue(Map<String, Object> body, String key) {
        Object value = body.get(key);
        return value == null ? null : value.toString();
    }


    private void apply(Context ctx, CallCenterWebView.HttpResult result) {
        try {
            ctx.status(result.getStatus());
            ctx.contentType("application/json; charset=utf-8");
            ctx.result(objectMapper.writeValueAsString(result.getBody()));
        } catch (Exception exception) {
            ctx.status(500);
            ctx.contentType("application/json; charset=utf-8");
            ctx.result("{\"message\":\"Сталася помилка сервера під час обробки запиту.\"}");
        }
    }
}