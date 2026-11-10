package callcenter;

public class QualityEvaluation {
    private final String evaluationId;
    private final int score;
    private final String feedback;

    public QualityEvaluation(String evaluationId, int score, String feedback) {
        this.evaluationId = evaluationId;
        this.score = score;
        this.feedback = feedback;
    }

    public String getResult() {
        return "Оцінка: " + score + ". " + feedback;
    }

    public String getEvaluationId() {
        return evaluationId;
    }

    public int getScore() {
        return score;
    }

    public String getFeedback() {
        return feedback;
    }
}
