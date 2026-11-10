package callcenter;

import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;

public final class ConsoleEncoding {
    private ConsoleEncoding() {
    }

    public static void configureUtf8() {
        try {
            System.setOut(new PrintStream(new FileOutputStream(FileDescriptor.out), true, "UTF-8"));
            System.setErr(new PrintStream(new FileOutputStream(FileDescriptor.err), true, "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException("Кодування UTF-8 не підтримується.", e);
        }
    }
}
