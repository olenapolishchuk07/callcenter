package callcenter;

public class Operator extends CallCenterParticipant {
    private final String operatorStatus;

    public Operator(String participantId, String displayName, String operatorStatus) {
        super(participantId, displayName);
        this.operatorStatus = operatorStatus;
    }

    @Override
    public String getDisplayName() {
        return displayName;
    }

    public void handleCall(Call call, ConversationScript script) {
        if (call == null || script == null) {
            throw new IllegalArgumentException("Дзвінок і сценарій розмови мають бути вказані.");
        }
        if (!call.start()) {
            System.out.println("Обробку дзвінка не розпочато. Стан: " + call.getState() + ".");
            return;
        }
        System.out.println("Стан дзвінка: " + call.getState() + ".");
        for (int step = 1; step <= script.getStepCount(); step++) {
            System.out.println("Оператор виконує крок " + step + ": " + script.executeStep(step));
        }
        CallRecording recording = call.completeAndRecord();
        if (recording == null) {
            System.out.println("Розмову не завершено. Стан дзвінка: " + call.getState() + ".");
            return;
        }
        System.out.println("Стан дзвінка: " + call.getState() + ".");
    }

    public boolean isAvailable() {
        return "available".equalsIgnoreCase(operatorStatus)
                || "доступний".equalsIgnoreCase(operatorStatus);
    }

    public String getOperatorStatus() {
        return operatorStatus;
    }
}
