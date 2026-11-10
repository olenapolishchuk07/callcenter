package callcenter;

import java.time.LocalDateTime;

public class Call {
    private final String callId;
    private final Client client;
    private final Operator operator;
    private final TelephonyService telephonyService;
    private String state = "NewRequest";
    private CallRecording recording;
    private QualityEvaluation qualityEvaluation;
    private ConversationSummary summary;

    public Call(String callId, Client client, Operator operator, TelephonyService telephonyService) {
        if (callId == null || callId.trim().isEmpty() || client == null || operator == null
                || telephonyService == null) {
            throw new IllegalArgumentException("Для дзвінка потрібні ідентифікатор, учасники та служба телефонії.");
        }
        this.callId = callId;
        this.client = client;
        this.operator = operator;
        this.telephonyService = telephonyService;
    }

    public boolean start() {
        if (!"NewRequest".equals(state)) {
            throw new IllegalStateException("Дзвінок можна розпочати лише для нової заявки.");
        }
        if (!operator.isAvailable()) {
            state = "OperatorUnavailable";
            return false;
        }
        if (!telephonyService.startCall(callId)) {
            state = "CallFailed";
            return false;
        }
        state = "CallInProgress";
        return true;
    }

    public CallRecording completeAndRecord() {
        if (!"CallInProgress".equals(state)) {
            throw new IllegalStateException("Записати можна лише дзвінок в обробці.");
        }
        if (!telephonyService.endCall(callId)) {
            state = "CallFailed";
            return null;
        }
        recording = new CallRecording(callId + "-REC", 120, LocalDateTime.now());
        state = "Recorded";
        return recording;
    }

    public void saveEvaluation(QualityEvaluation evaluation) {
        if (!"Recorded".equals(state) || recording == null || evaluation == null) {
            throw new IllegalStateException("Оцінку можна зберегти лише для дзвінка з доступним записом.");
        }
        qualityEvaluation = evaluation;
        state = "QualityEvaluated";
    }

    public void saveSummary(ConversationSummary summary) {
        if (!"QualityEvaluated".equals(state) || summary == null) {
            throw new IllegalStateException("Підсумок можна підготувати після оцінювання якості.");
        }
        this.summary = summary;
    }

    public void confirmSummarySent(boolean success) {
        if (!"QualityEvaluated".equals(state)) {
            throw new IllegalStateException("Надсилання підсумку підтверджується після оцінювання.");
        }
        state = success && summary != null ? "SummarySent" : "SummaryFailed";
    }

    public String getCallId() {
        return callId;
    }

    public String getState() {
        return state;
    }

    public Client getClient() {
        return client;
    }

    public Operator getOperator() {
        return operator;
    }

    public CallRecording getRecording() {
        return recording;
    }

    public QualityEvaluation getQualityEvaluation() {
        return qualityEvaluation;
    }

    public ConversationSummary getSummary() {
        return summary;
    }
}
