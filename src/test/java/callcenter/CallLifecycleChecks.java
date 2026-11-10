package callcenter;

import java.time.LocalDateTime;
import java.util.Arrays;

public class CallLifecycleChecks {
    public static void main(String[] args) {
        checkEvaluationWithoutRecording();
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
        require(!"QualityEvaluated".equals(call.getState()), "Без запису не можна перейти до QualityEvaluated.");
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
        expectIllegalArgument(new Client("C1", "Клієнт", null, "000"),
                "Відсутня адреса електронної пошти має бути відхилена.");
        expectIllegalArgument(new Client("C2", "Клієнт", " ", "000"),
                "Порожня адреса електронної пошти має бути відхилена.");
        expectIllegalArgument(new Client("C3", "Клієнт", "test@example.invalid", null),
                "Відсутній номер телефону має бути відхилений.");
        expectIllegalArgument(new Client("C4", "Клієнт", "test@example.invalid", "  "),
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

    private static void expectIllegalArgument(Client client, String message) {
        boolean rejected = false;
        try {
            client.requestCallback("CHECK-CONTACT", availableOperator(), new TelephonyService("Перевірка"));
        } catch (IllegalArgumentException expected) {
            rejected = true;
        }
        require(rejected, message);
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
