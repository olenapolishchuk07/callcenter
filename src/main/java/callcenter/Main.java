package callcenter;

import java.time.LocalDateTime;
import java.util.Arrays;

public class Main {
    public static void main(String[] args) {
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
        Call call = client.requestCallback("CALL-001", "сьогодні", operator, telephony);
        System.out.println("Стан дзвінка: " + call.getState() + ".");

        operator.handleCall(call, script);
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
        System.out.println("Демонстраційний сценарій завершено.");
    }
}
