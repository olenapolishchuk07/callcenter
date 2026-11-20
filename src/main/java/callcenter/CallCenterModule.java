package callcenter;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;

public class CallCenterModule extends AbstractModule {
    private final String callId;
    private final Client client;
    private final Operator operator;
    private final TelephonyService telephonyService;

    public CallCenterModule(String callId, Client client, Operator operator, TelephonyService telephonyService) {
        this.callId = callId;
        this.client = client;
        this.operator = operator;
        this.telephonyService = telephonyService;
    }

    @Override
    protected void configure() {
        bind(RecordingStore.class).to(SqliteRecordingStore.class).in(Singleton.class);
        bind(WebServer.class).to(JavalinWebServer.class).in(Singleton.class);
        bind(String.class).annotatedWith(com.google.inject.name.Names.named("callId")).toInstance(callId);
        bind(Client.class).toInstance(client);
        bind(Operator.class).toInstance(operator);
        bind(TelephonyService.class).toInstance(telephonyService);
    }

    @Provides
    @Singleton
    Connection provideConnection() {
        try {
            Path target = Path.of("target");
            Files.createDirectories(target);
            return DriverManager.getConnection("jdbc:sqlite:" + target.resolve("callcenter.db"));
        } catch (Exception exception) {
            throw new IllegalStateException("Помилка підключення до бази даних SQLite: "
                    + exception.getMessage(), exception);
        }
    }
}
