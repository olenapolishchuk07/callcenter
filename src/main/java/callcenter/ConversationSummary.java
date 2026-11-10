package callcenter;

import java.time.LocalDateTime;

public class ConversationSummary {
    private final String summaryId;
    private final String content;
    private final LocalDateTime preparedAt;

    public ConversationSummary(String summaryId, String content, LocalDateTime preparedAt) {
        this.summaryId = summaryId;
        this.content = content;
        this.preparedAt = preparedAt;
    }

    public String getContent() {
        return content;
    }

    public String getSummaryId() {
        return summaryId;
    }

    public LocalDateTime getPreparedAt() {
        return preparedAt;
    }
}
