package callcenter;

public class CallCenterWebView {
    public static final class HttpResult {
        private final int status;
        private final Object body;

        public HttpResult(int status, Object body) {
            this.status = status;
            this.body = body;
        }

        public int getStatus() {
            return status;
        }

        public int getStatusCode() {
            return status;
        }

        public Object getBody() {
            return body;
        }
    }

    public HttpResult read(CallCenterOperationResult result) {
        if (!result.isSuccess()) {
            return failure(result);
        }
        return new HttpResult(200, result.getRecordings());
    }

    public HttpResult create(CallCenterOperationResult result) {
        if (!result.isSuccess()) {
            return failure(result);
        }
        return new HttpResult(201, new Message(result.getMessage()));
    }

    public HttpResult update(CallCenterOperationResult result) {
        if (!result.isSuccess()) {
            return failure(result);
        }
        return new HttpResult(200, new Message(result.getMessage()));
    }

    public HttpResult delete(CallCenterOperationResult result) {
        if (!result.isSuccess()) {
            return failure(result);
        }
        return new HttpResult(200, new Message(result.getMessage()));
    }

    public HttpResult failure(CallCenterOperationResult result) {
        int status = switch (result.getType()) {
            case INVALID -> 400;
            case NOT_FOUND -> 404;
            case CONFLICT -> 409;
            case STORAGE_ERROR -> 500;
            default -> 400;
        };
        return new HttpResult(status, new Message(result.getMessage()));
    }

    public static class Message {
        private final String message;

        public Message(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }
    }
}
