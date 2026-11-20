package callcenter;

import com.google.inject.Guice;
import com.google.inject.Injector;

public class Main {
    public static void main(String[] args) {
        Client client = new Client("CLIENT-001", "Олена Демонстраційна",
                "olena@example.invalid", "+380000000000");
        Operator operator = new Operator("OPERATOR-001", "Андрій Оператор", "Доступний");
        TelephonyService telephony = new TelephonyService("Симульована телефонія");
        Injector injector = Guice.createInjector(
                new CallCenterModule("CALL-001", client, operator, telephony));
        Call call = injector.getInstance(Call.class);
        CallCenterController controller = new CallCenterController(call);
        WebServer webServer = injector.getInstance(WebServer.class);
        System.out.println("Вебсервер Call Center запущено на http://localhost:8080/");
        webServer.start(controller, 8080);
    }
}
