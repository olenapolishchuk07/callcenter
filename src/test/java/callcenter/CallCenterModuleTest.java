package callcenter;

import com.google.inject.Guice;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class CallCenterModuleTest {
    @Test
    void guiceBindsStoreAndCreatesCall() {
        Client client = new Client("C1", "Клієнт", "test@example.invalid", "000");
        Operator operator = new Operator("O1", "Оператор", "Доступний");
        TelephonyService telephony = new TelephonyService("Перевірка");
        var injector = Guice.createInjector(
                new CallCenterModule("CALL-GUICE", client, operator, telephony));

        assertInstanceOf(SqliteRecordingStore.class,
                injector.getInstance(RecordingStore.class));
        Call call = injector.getInstance(Call.class);
        assertInstanceOf(Call.class, call);
    }
}
