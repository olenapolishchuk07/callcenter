package callcenter;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

class CallCenterWebViewTest {
    @Test
    void mapsModelResultsToHttpStatuses() {
        CallCenterWebView view = new CallCenterWebView();
        assertEquals(400, view.failure(CallCenterOperationResult.failure(
                CallCenterOperationResult.Type.INVALID, "Некоректні дані.")).getStatus());
        assertEquals(404, view.failure(CallCenterOperationResult.failure(
                CallCenterOperationResult.Type.NOT_FOUND, "Запис не знайдено.")).getStatus());
        assertEquals(409, view.failure(CallCenterOperationResult.failure(
                CallCenterOperationResult.Type.CONFLICT, "Заборонено.")).getStatus());
        assertEquals(500, view.failure(CallCenterOperationResult.failure(
                CallCenterOperationResult.Type.STORAGE_ERROR, "Помилка сховища.")).getStatus());
    }
}
