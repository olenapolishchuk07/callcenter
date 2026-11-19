package callcenter;

import org.junit.jupiter.api.Test;

class CallLifecycleTest {
    @Test
    void existingLifecycleChecksRemainValid() {
        CallLifecycleChecks.main(new String[0]);
    }
}
