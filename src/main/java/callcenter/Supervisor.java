package callcenter;

public class Supervisor extends CallCenterParticipant {
    private final String supervisorCode;

    public Supervisor(String participantId, String displayName, String supervisorCode) {
        super(participantId, displayName);
        this.supervisorCode = supervisorCode;
    }

    @Override
    public String getDisplayName() {
        return displayName;
    }

    public QualityEvaluation evaluate(CallRecording recording, Call call) {
        if (call == null || !"Recorded".equals(call.getState())) {
            System.out.println("Оцінювання неможливе: доступного запису для дзвінка немає.");
            return null;
        }
        if (recording == null || call.getRecording() != recording) {
            call.failEvaluation();
            System.out.println("Оцінювання неможливе: доступного запису для дзвінка немає."
                    + " Стан дзвінка: " + call.getState() + ".");
            return null;
        }
        String details = recording.listen();
        QualityEvaluation evaluation = new QualityEvaluation(
                call.getCallId() + "-EVAL", 95, "Розмову оцінено; " + details);
        call.saveEvaluation(evaluation);
        System.out.println("Супервізор " + displayName + " оцінив якість розмови.");
        return evaluation;
    }

    public String getSupervisorCode() {
        return supervisorCode;
    }
}
