package callcenter;

public interface WebServer {
    void start(CallCenterController controller, CallCenterWebView view, int port);
    void stop();
}
