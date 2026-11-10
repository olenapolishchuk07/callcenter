package callcenter;

public interface ConversationScript {
    String executeStep(int stepNumber);

    int getStepCount();
}
