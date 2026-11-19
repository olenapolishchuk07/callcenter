package callcenter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class HtmlReportGenerator {
    public void generate(List<CallRecording> recordings) {
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html>\n<html lang=\"uk\"><head><meta charset=\"UTF-8\">")
                .append("<title>Звіт кол-центру</title>")
                .append("<style>body{font-family:Arial,Helvetica,sans-serif;color:#222;}")
                .append("table{border-collapse:collapse;}th,td{border:1px solid #ccc;padding:8px 12px;}")
                .append("th{background-color:#f2f4f7;text-align:left;}</style></head><body>\n")
                .append("<h1>Записи розмов кол-центру</h1>\n")
                .append("<table>\n<thead><tr><th>recordingId</th><th>duration</th><th>createdAt</th></tr></thead>\n")
                .append("<tbody>\n");
        if (recordings != null) {
            for (CallRecording recording : recordings) {
                html.append("<tr><td>").append(escape(recording.getRecordingId()))
                        .append("</td><td>").append(recording.getDuration())
                        .append("</td><td>").append(escape(recording.getCreatedAt().toString()))
                        .append("</td></tr>\n");
            }
        }
        html.append("</tbody>\n</table>\n</body></html>\n");
        try {
            Files.createDirectories(Path.of("target"));
            Files.writeString(Path.of("target/callcenter-report.html"), html, StandardCharsets.UTF_8);
        } catch (IOException exception) {
            throw new IllegalStateException("Помилка генерації HTML-звіту: "
                    + exception.getMessage(), exception);
        }
    }

    public static String escape(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}
