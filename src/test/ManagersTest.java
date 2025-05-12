package test;

import manager.Managers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ManagerTest {
    ManagerTest() {
    }

    @Test
    void shouldCreateDefaultManager() {
        Assertions.assertNotNull(Managers.getDefault());
    }

    @Test
    void shouldCreateDefaultHistoryManager() {
        Assertions.assertNotNull(Managers.getDefaultHistory());
    }

    @Test
    public void shouldCorrectlyAssembleProgramm() {
        Assertions.assertNotNull(Managers.getDefault());
        Assertions.assertNotNull(Managers.getDefaultHistory());
    }
}