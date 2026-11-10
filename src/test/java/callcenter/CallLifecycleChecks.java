package callcenter;

import java.time.LocalDateTime;
import java.util.Arrays;

public class CallLifecycleChecks {
    public static void main(String[] args) {
        ConsoleEncoding.configureUtf8();
        checkEvaluationWithoutRecording();
        checkUnavailableRecording();
        checkUnfinishedCall();
        checkMissingContactDetails();
        checkNegativeRecordingDuration();
        checkSuccessfulLifecycle();
        System.out.println("Усі окремі перевірки життєвого циклу пройдено.");
    }

    private static void checkEvaluationWithoutRecording() {
        Client client = validClient();
        Call call = new Call("CHECK-NO-RECORDING", client, availableOperator(), new TelephonyService("Перевірка"));
        QualityEvaluation result = new Supervisor("SUP-CHECK", "Перевіряльник", "SUP-CHECK")
                .evaluate(null, call);
        require(result == null, "Оцінювання без запису не має створювати результат.");
        require(call.getQualityEvaluation() == null, "Оцінювання без запису не має зберігатися.");
        require("NewRequest".equals(call.getState()),
                "Спроба оцінювання до створення запису не має змінювати стан заявки.");
    }

    private static void checkUnavailableRecording() {
        Call call = validClient().requestCallback("CHECK-UNAVAILABLE-RECORDING", availableOperator(),
                new TelephonyService("Перевірка"));
        new Operator("OP-CHECK", "Оператор перевірки", "Доступний").handleCall(call,
                new StandardConversationScript("Перевірка", Arrays.asList("Завершити дзвінок.")));
        require("Recorded".equals(call.getState()), "Передумова перевірки має створити запис.");

        QualityEvaluation result = new Supervisor("SUP-CHECK", "Перевіряльник", "SUP-CHECK")
                .evaluate(null, call);
        require(result == null, "Недоступний запис не має створювати оцінку.");
        require(call.getQualityEvaluation() == null, "Оцінка для недоступного запису не має зберігатися.");
        require("EvaluationFailed".equals(call.getState()),
                "Недоступний запис має перевести дзвінок у EvaluationFailed.");
    }

    private static void checkUnfinishedCall() {
        TelephonyService telephony = new TelephonyService("Перевірка") {
            @Override
            public boolean endCall(String callId) {
                System.out.println("Перевірка: завершення дзвінка не підтверджено.");
                return false;
            }
        };
        Call call = new Call("CHECK-FAILED-CALL", validClient(), availableOperator(), telephony);
        ConversationScript script = new StandardConversationScript("Перевірка",
                Arrays.asList("Виконати перевірочний крок."));
        new Operator("OP-CHECK", "Оператор перевірки", "Доступний").handleCall(call, script);
        require(call.getRecording() == null, "Незавершений дзвінок не має створювати запис.");
        require(!"Recorded".equals(call.getState()), "Незавершений дзвінок не має переходити до Recorded.");
        require(!"QualityEvaluated".equals(call.getState()),
                "Незавершений дзвінок не має переходити до QualityEvaluated.");
        require(call.getQualityEvaluation() == null, "Незавершений дзвінок не має мати оцінки.");
    }

    private static void checkMissingContactDetails() {
        expectRejectedRequest(new Client("C1", "Клієнт", null, "000"),
                "Відсутня адреса електронної пошти має бути відхилена.");
        expectRejectedRequest(new Client("C2", "Клієнт", " ", "000"),
                "Порожня адреса електронної пошти має бути відхилена.");
        expectRejectedRequest(new Client("C3", "Клієнт", "test@example.invalid", null),
                "Відсутній номер телефону має бути відхилений.");
        expectRejectedRequest(new Client("C4", "Клієнт", "test@example.invalid", "  "),
                "Порожній номер телефону має бути відхилений.");
    }

    private static void checkNegativeRecordingDuration() {
        boolean rejected = false;
        try {
            new CallRecording("CHECK-NEGATIVE", -1, LocalDateTime.now());
        } catch (IllegalArgumentException expected) {
            rejected = true;
        }
        require(rejected, "Від'ємна тривалість запису має бути відхилена.");
    }

    private static void checkSuccessfulLifecycle() {
        Operator operator = availableOperator();
        Call call = validClient().requestCallback("CHECK-SUCCESS", operator,
                new TelephonyService("Перевірка"));
        operator.handleCall(call, new StandardConversationScript("Перевірка",
                Arrays.asList("Завершити успішний сценарій.")));
        require("Recorded".equals(call.getState()), "Успішна обробка має створити запис.");
        Supervisor supervisor = new Supervisor("SUP-CHECK", "Перевіряльник", "SUP-CHECK");
        require(supervisor.evaluate(call.getRecording(), call) != null,
                "Доступний запис має бути оцінений.");
        require("QualityEvaluated".equals(call.getState()), "Оцінювання має зберегти відповідний стан.");
    }

    private static Client validClient() {
        return new Client("CLIENT-CHECK", "Клієнт перевірки",
                "check@example.invalid", "+380000000000");
    }

    private static Operator availableOperator() {
        return new Operator("OP-CHECK", "Оператор перевірки", "Доступний");
    }

    private static void expectRejectedRequest(Client client, String message) {
        Call call = client.requestCallback("CHECK-CONTACT", availableOperator(), new TelephonyService("Перевірка"));
        require("RequestRejected".equals(call.getState()), message);
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
