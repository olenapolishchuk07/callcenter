package callcenter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class StandardConversationScript implements ConversationScript {
    private final String scriptName;
    private final List<String> steps;

    public StandardConversationScript(String scriptName, List<String> steps) {
        if (scriptName == null || scriptName.trim().isEmpty()) {
            throw new IllegalArgumentException("Назва сценарію не може бути порожньою.");
        }
        if (steps == null || steps.isEmpty()) {
            throw new IllegalArgumentException("Сценарій має містити принаймні один крок.");
        }
        this.scriptName = scriptName;
        this.steps = Collections.unmodifiableList(new ArrayList<String>(steps));
    }

    @Override
    public String executeStep(int stepNumber) {
        if (stepNumber < 1 || stepNumber > steps.size()) {
            throw new IllegalArgumentException("Номер кроку виходить за межі сценарію.");
        }
        return steps.get(stepNumber - 1);
    }

    @Override
    public int getStepCount() {
        return steps.size();
    }

    @Override
    public String getScriptName() {
        return scriptName;
    }
}
