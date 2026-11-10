package callcenter;

public class EmailService {
    private final String serviceName;

    public EmailService(String serviceName) {
        this.serviceName = serviceName;
    }

    public boolean sendSummary(Client client, ConversationSummary summary) {
        if (client == null || summary == null) {
            throw new IllegalArgumentException("Клієнт і підсумок мають бути вказані.");
        }
        System.out.println("Симуляція електронної пошти: підсумок надіслано на "
                + client.getEmail() + ".");
        client.receiveSummary(summary);
        return true;
    }

    public String getServiceName() {
        return serviceName;
    }
}
