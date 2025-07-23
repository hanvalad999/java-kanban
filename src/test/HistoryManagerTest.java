package test;

import manager.HistoryManager;
import manager.Managers;
import model.Task;
import model.Status;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class HistoryManagerTest {

    @Test
    void newHistoryManager() {
        HistoryManager historyManager = Managers.getDefaultHistory();

        assertNotNull(historyManager, "Менеджер не проинициализирован");
    }

    @Test
    void add() {
        HistoryManager historyManager = Managers.getDefaultHistory();
        Task task = new Task("Test", "Description", 1, Status.NEW);
        historyManager.add(task);

        List<Task> history = historyManager.getHistory();
        assertNotNull(history, "История не пустая.");
        assertEquals(1, history.size(), "История не пустая.");
        assertEquals(task, history.get(0), "Добавленная задача должна быть в истории");
    }

    @Test
    void shouldNotStoreDuplicates() {
        HistoryManager historyManager = Managers.getDefaultHistory();
        Task task = new Task("Test", "Description", 1, Status.NEW);

        for (int i = 0; i < 10; i++) {
            historyManager.add(task);
        }

        List<Task> history = historyManager.getHistory();
        assertEquals(1, history.size(), "История не должна содержать дубликаты");
    }

    @Test
    void shouldRemoveTaskFromHistory() {
        HistoryManager historyManager = Managers.getDefaultHistory();
        Task task = new Task("Task to Remove", "desc", 1, Status.NEW);

        historyManager.add(task);
        historyManager.remove(1);

        List<Task> history = historyManager.getHistory();
        assertTrue(history.isEmpty(), "История должна быть пустой после удаления задачи");
    }
}