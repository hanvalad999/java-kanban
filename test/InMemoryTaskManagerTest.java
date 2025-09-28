package test;

import manager.InMemoryHistoryManager;
import manager.InMemoryTaskManager;

public class InMemoryTaskManagerTest extends AbstractTaskManagerTest<InMemoryTaskManager> {
    @Override
    protected InMemoryTaskManager createManager() {
        return new InMemoryTaskManager(new InMemoryHistoryManager());
    }
}