package callcenter;

public interface WebServer {
    void start(CallCenterController controller, int port);
    void stop();
}
