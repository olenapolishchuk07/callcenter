package callcenter;

import java.time.LocalDateTime;
import java.util.Arrays;
import com.google.inject.Guice;
import com.google.inject.Injector;

public class Main {
    public static void main(String[] args) {
        ConsoleEncoding.configureUtf8();
        Client client = new Client("CLIENT-001", "Олена Демонстраційна",
                "olena@example.invalid", "+380000000000");
        Operator operator = new Operator("OPERATOR-001", "Андрій Оператор", "Доступний");
        Supervisor supervisor = new Supervisor("SUPERVISOR-001", "Марія Супервізор", "SUP-001");
        TelephonyService telephony = new TelephonyService("Симульована телефонія");
        EmailService email = new EmailService("Симульована електронна пошта");
        ConversationScript script = new StandardConversationScript("Стандартна консультація",
                Arrays.asList("Вітання та уточнення потреби клієнта.",
                        "Надання консультації та перевірка розуміння.",
                        "Підбиття підсумків розмови."));

        System.out.println("Демонстрація обробки звернення клієнта.");
        Injector injector;
        try {
            injector = Guice.createInjector(new CallCenterModule("CALL-001", client, operator, telephony));
        } catch (Exception exception) {
            System.out.println("Помилка підключення до бази даних SQLite: " + exception.getMessage());
            return;
        }
        Call call = injector.getInstance(Call.class);
        client.requestCallback("CALL-001", "сьогодні", call);
        System.out.println("Стан дзвінка: " + call.getState() + ".");

        try {
            operator.handleCall(call, script);
        } catch (IllegalStateException exception) {
            System.out.println(exception.getMessage());
            return;
        }
        if (call.getRecording() == null) {
            throw new IllegalStateException("Демонстраційний дзвінок не вдалося записати.");
        }

        QualityEvaluation evaluation = supervisor.evaluate(call.getRecording(), call);
        if (evaluation == null) {
            throw new IllegalStateException("Демонстраційний дзвінок не вдалося оцінити.");
        }
        System.out.println("Стан дзвінка: " + call.getState() + ".");

        ConversationSummary summary = new ConversationSummary(call.getCallId() + "-SUMMARY",
                "Клієнт отримав консультацію; питання опрацьовано.", LocalDateTime.now());
        call.saveSummary(summary);
        boolean sent = email.sendSummary(client, summary);
        call.confirmSummarySent(sent);
        if (!"SummarySent".equals(call.getState())) {
            throw new IllegalStateException("Не вдалося підтвердити надсилання підсумку.");
        }
        System.out.println("Стан дзвінка: " + call.getState() + ".");

        try {
            RecordingStore store = injector.getInstance(RecordingStore.class);
            new HtmlReportGenerator().generate(store.findAll());
        } catch (IllegalStateException exception) {
            System.out.println(exception.getMessage());
            return;
        }
        System.out.println("Звіт створено у target/callcenter-report.html.");
        System.out.println("Демонстраційний сценарій завершено.");
    }
}
