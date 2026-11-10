package callcenter;

public class TelephonyService {
    private final String serviceName;

    public TelephonyService(String serviceName) {
        this.serviceName = serviceName;
    }

    public boolean startCall(String callId) {
        System.out.println("Симуляція телефонії: дзвінок " + callId + " розпочато.");
        return true;
    }

    public boolean endCall(String callId) {
        System.out.println("Симуляція телефонії: дзвінок " + callId + " завершено.");
        return true;
    }

    public String getServiceName() {
        return serviceName;
    }
}
