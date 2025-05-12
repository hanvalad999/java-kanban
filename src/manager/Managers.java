package manager;

public class Managers {
    public static TaskManager getDefault() {
        return new InMemoryTaskManager(); // или другая реализация
    }

    public static HistoryManager getDefaultHistory() {
        return new InMemoryHistoryManager();
    }
}
