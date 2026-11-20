package callcenter;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Call {
    private final String callId;
    private final Client client;
    private final Operator operator;
    private final TelephonyService telephonyService;
    private String state = "NewRequest";
    private CallRecording recording;
    private final List<CallRecording> recordings = new ArrayList<>();
    private QualityEvaluation qualityEvaluation;
    private ConversationSummary summary;
    private final RecordingStore recordingStore;

    public Call(String callId, Client client, Operator operator, TelephonyService telephonyService) {
        this(callId, client, operator, telephonyService, null);
    }

    @Inject
    public Call(@Named("callId") String callId, Client client, Operator operator, TelephonyService telephonyService,
            RecordingStore recordingStore) {
        if (callId == null || callId.trim().isEmpty() || client == null || operator == null
                || telephonyService == null) {
            throw new IllegalArgumentException("Для дзвінка потрібні ідентифікатор, учасники та служба телефонії.");
        }
        this.callId = callId;
        this.client = client;
        this.operator = operator;
        this.telephonyService = telephonyService;
        this.recordingStore = recordingStore;
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

    public void rejectRequest() {
        if (!"NewRequest".equals(state)) {
            throw new IllegalStateException("Відхилити можна лише нову заявку.");
        }
        state = "RequestRejected";
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
        if (recordingStore != null) {
            recordingStore.insert(recording);
        }
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

    public void failEvaluation() {
        if (!"Recorded".equals(state)) {
            throw new IllegalStateException("Неуспішне оцінювання можливе лише після створення запису.");
        }
        state = "EvaluationFailed";
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

    public List<CallRecording> getRecordings() {
        if (recordingStore == null) {
            return new ArrayList<>(recordings);
        }
        List<CallRecording> stored = recordingStore.findAll();
        recordings.clear();
        recordings.addAll(stored);
        return new ArrayList<>(recordings);
    }

    public CallCenterOperationResult findAllRecordings() {
        try {
            return CallCenterOperationResult.success("Записи прочитано.", getRecordings());
        } catch (IllegalStateException exception) {
            return CallCenterOperationResult.failure(
                    CallCenterOperationResult.Type.STORAGE_ERROR, "Не вдалося прочитати записи зі сховища.");
        }
    }

    private boolean changesForbidden() {
        if (!operator.isAvailable()) {
            return true;
        }
        return "RequestRejected".equals(state) || "CallFailed".equals(state)
                || "QualityEvaluated".equals(state) || "SummarySent".equals(state);
    }

    public CallCenterOperationResult addRecording(CallRecording recording) {
        if (recording == null) {
            return CallCenterOperationResult.failure(
                    CallCenterOperationResult.Type.INVALID, "Дані запису є некоректними.");
        }
        if (changesForbidden()) {
            return CallCenterOperationResult.failure(
                    CallCenterOperationResult.Type.CONFLICT, "Зміну записів заборонено поточним станом дзвінка.");
        }
        try {
            if (recordingStore.findById(recording.getRecordingId()) != null) {
                return CallCenterOperationResult.failure(
                        CallCenterOperationResult.Type.CONFLICT, "Запис із таким recordingId вже існує.");
            }
            recordingStore.insert(recording);
            getRecordings();
            return CallCenterOperationResult.success("Запис успішно додано.", recording);
        } catch (IllegalArgumentException exception) {
            if (exception.getMessage().contains("вже існує")) {
                return CallCenterOperationResult.failure(
                        CallCenterOperationResult.Type.CONFLICT, "Запис із таким recordingId вже існує.");
            }
            return CallCenterOperationResult.failure(
                    CallCenterOperationResult.Type.INVALID, exception.getMessage());
        } catch (IllegalStateException exception) {
            return CallCenterOperationResult.failure(
                    CallCenterOperationResult.Type.STORAGE_ERROR,
                    "Не вдалося зберегти запис через помилку сховища.");
        }
    }

    public CallCenterOperationResult addRecording(String recordingId, Object duration, String createdAt) {
        try {
            return addRecording(new CallRecording(recordingId, duration, createdAt));
        } catch (IllegalArgumentException exception) {
            return CallCenterOperationResult.failure(
                    CallCenterOperationResult.Type.INVALID, exception.getMessage());
        }
    }

    public CallCenterOperationResult updateRecording(String recordingId, CallRecording recording) {
        try {
            if (recordingId == null || recordingStore.findById(recordingId) == null) {
                return CallCenterOperationResult.failure(
                        CallCenterOperationResult.Type.NOT_FOUND, "Запис не знайдено.");
            }
            if (recording == null
                    || !recordingId.trim().equals(recording.getRecordingId())) {
                return CallCenterOperationResult.failure(
                        CallCenterOperationResult.Type.INVALID,
                        "Ідентифікатор запису в тілі й адресі має збігатися.");
            }
            if (changesForbidden()) {
                return CallCenterOperationResult.failure(
                        CallCenterOperationResult.Type.CONFLICT,
                        "Зміну записів заборонено поточним станом дзвінка.");
            }
            recordingStore.update(recording);
            getRecordings();
            return CallCenterOperationResult.success("Запис успішно виправлено.", recording);
        } catch (IllegalArgumentException exception) {
            return CallCenterOperationResult.failure(
                    CallCenterOperationResult.Type.INVALID, exception.getMessage());
        } catch (IllegalStateException exception) {
            return CallCenterOperationResult.failure(
                    CallCenterOperationResult.Type.STORAGE_ERROR,
                    "Не вдалося оновити запис через помилку сховища.");
        }
    }

    public CallCenterOperationResult updateRecording(
            String pathId, String recordingId, Object duration, String createdAt) {
        try {
            if (pathId == null || recordingStore.findById(pathId) == null) {
                return CallCenterOperationResult.failure(
                        CallCenterOperationResult.Type.NOT_FOUND, "Запис не знайдено.");
            }
            return updateRecording(pathId,
                    new CallRecording(recordingId, duration, createdAt));
        } catch (IllegalArgumentException exception) {
            if (exception.getMessage().contains("Ідентифікатор запису")) {
                return CallCenterOperationResult.failure(
                        CallCenterOperationResult.Type.INVALID, exception.getMessage());
            }
            return CallCenterOperationResult.failure(
                    CallCenterOperationResult.Type.INVALID, exception.getMessage());
        }
    }

    public CallCenterOperationResult deleteRecording(String recordingId) {
        if (recordingId == null || recordingId.trim().isEmpty()) {
            return CallCenterOperationResult.failure(
                    CallCenterOperationResult.Type.INVALID, "Ідентифікатор запису є обов'язковим.");
        }
        try {
            if (recordingStore.findById(recordingId) == null) {
                return CallCenterOperationResult.failure(
                        CallCenterOperationResult.Type.NOT_FOUND, "Запис не знайдено.");
            }
            if (changesForbidden()) {
                return CallCenterOperationResult.failure(
                        CallCenterOperationResult.Type.CONFLICT,
                        "Зміну записів заборонено поточним станом дзвінка.");
            }
            if (!recordingStore.delete(recordingId)) {
                return CallCenterOperationResult.failure(
                        CallCenterOperationResult.Type.NOT_FOUND, "Запис не знайдено.");
            }
            getRecordings();
            return CallCenterOperationResult.success("Запис успішно видалено.");
        } catch (IllegalStateException exception) {
            return CallCenterOperationResult.failure(
                    CallCenterOperationResult.Type.STORAGE_ERROR,
                    "Не вдалося видалити запис через помилку сховища.");
        }
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
