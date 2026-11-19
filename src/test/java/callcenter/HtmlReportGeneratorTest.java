package callcenter;

import org.junit.jupiter.api.Test;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;

class HtmlReportGeneratorTest {
    @Test
    void generatesEmptyAndEscapedHtmlTables() throws Exception {
        Path report = Path.of("target/callcenter-report.html");
        HtmlReportGenerator generator = new HtmlReportGenerator();

        generator.generate(List.of());
        String empty = Files.readString(report, StandardCharsets.UTF_8);
        assertTrue(empty.contains("<h1>Записи розмов кол-центру</h1>"));
        assertTrue(empty.contains("table{border-collapse:collapse;}"));
        assertTrue(empty.contains("th,td{border:1px solid #ccc;padding:8px 12px;}"));
        assertTrue(empty.contains("th{background-color:#f2f4f7;text-align:left;}"));
        assertTrue(empty.contains("<th>recordingId</th>"));
        assertTrue(empty.contains("<th>duration</th>"));
        assertTrue(empty.contains("<th>createdAt</th>"));
        assertTrue(empty.contains("<tbody>\n</tbody>"));

        CallRecording recording = new CallRecording(
                "<>&\"'", 12, LocalDateTime.of(2026, 11, 19, 20, 4));
        generator.generate(List.of(recording));
        String escaped = Files.readString(report, StandardCharsets.UTF_8);
        assertTrue(escaped.contains("&lt;&gt;&amp;&quot;&#39;"));
        assertTrue(!escaped.contains("<td><>&\"'</td>"));
    }

    @Test
    void reportsUkrainianHtmlGenerationErrorAndStops() throws Exception {
        Path target = Path.of("target");
        Path report = target.resolve("callcenter-report.html");
        Files.createDirectories(target);
        Files.deleteIfExists(report);
        Files.createDirectory(report);

        try {
            IllegalStateException exception = assertThrows(IllegalStateException.class,
                    () -> new HtmlReportGenerator().generate(List.of()));
            assertEquals(true, exception.getMessage().startsWith("Помилка генерації HTML-звіту:"));
        } finally {
            Files.deleteIfExists(report);
        }
    }
}
