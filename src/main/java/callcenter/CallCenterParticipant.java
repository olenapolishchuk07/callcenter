package callcenter;

public abstract class CallCenterParticipant {
    private final String participantId;
    protected final String displayName;

    protected CallCenterParticipant(String participantId, String displayName) {
        this.participantId = participantId;
        this.displayName = displayName;
    }

    public final String getParticipantId() {
        return participantId;
    }

    public abstract String getDisplayName();
}
