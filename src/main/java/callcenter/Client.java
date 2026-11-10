package callcenter;

public class Client extends CallCenterParticipant {
    private final String email;
    private final String phoneNumber;

    public Client(String participantId, String displayName, String email, String phoneNumber) {
        super(participantId, displayName);
        this.email = email;
        this.phoneNumber = phoneNumber;
    }

    @Override
    public String getDisplayName() {
        return displayName;
    }

    public Call requestCallback(String callId, Operator operator, TelephonyService telephonyService) {
        return requestCallback(callId, null, operator, telephonyService);
    }

    public Call requestCallback(String callId, String preferredTime, Operator operator,
            TelephonyService telephonyService) {
        Call call = new Call(callId, this, operator, telephonyService);
        if (!hasContactDetails()) {
            call.rejectRequest();
            System.out.println("Заявку відхилено: не вказано адресу електронної пошти або номер телефону."
                    + " Стан дзвінка: " + call.getState() + ".");
            return call;
        }
        if (preferredTime != null && !preferredTime.trim().isEmpty()) {
            System.out.println("Клієнт " + displayName + " просить зворотний дзвінок: " + preferredTime + ".");
        } else {
            System.out.println("Клієнт " + displayName + " подав заявку на зворотний дзвінок.");
        }
        return call;
    }

    public void receiveSummary(ConversationSummary summary) {
        if (summary == null) {
            throw new IllegalArgumentException("Підсумок не може бути відсутнім.");
        }
        System.out.println("Клієнт " + displayName + " отримав підсумок розмови.");
    }

    public String getEmail() {
        return email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    private boolean hasContactDetails() {
        return email != null && !email.trim().isEmpty()
                && phoneNumber != null && !phoneNumber.trim().isEmpty();
    }
}
