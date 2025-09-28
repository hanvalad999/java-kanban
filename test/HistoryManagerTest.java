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

    @Test
    void shouldRemoveFirstTask() {
        HistoryManager historyManager = Managers.getDefaultHistory();
        Task t1 = new Task("T1", "D1", 1, Status.NEW);
        Task t2 = new Task("T2", "D2", 2, Status.NEW);
        Task t3 = new Task("T3", "D3", 3, Status.NEW);

        historyManager.add(t1);
        historyManager.add(t2);
        historyManager.add(t3);

        historyManager.remove(1);
        List<Task> history = historyManager.getHistory();

        assertEquals(List.of(t2, t3), history);
    }

    @Test
    void shouldRemoveMiddleTask() {
        HistoryManager historyManager = Managers.getDefaultHistory();
        Task t1 = new Task("T1", "D1", 1, Status.NEW);
        Task t2 = new Task("T2", "D2", 2, Status.NEW);
        Task t3 = new Task("T3", "D3", 3, Status.NEW);

        historyManager.add(t1);
        historyManager.add(t2);
        historyManager.add(t3);

        historyManager.remove(2);
        List<Task> history = historyManager.getHistory();

        assertEquals(List.of(t1, t3), history);
    }

    @Test
    void shouldRemoveLastTask() {
        HistoryManager historyManager = Managers.getDefaultHistory();
        Task t1 = new Task("T1", "D1", 1, Status.NEW);
        Task t2 = new Task("T2", "D2", 2, Status.NEW);
        Task t3 = new Task("T3", "D3", 3, Status.NEW);

        historyManager.add(t1);
        historyManager.add(t2);
        historyManager.add(t3);

        historyManager.remove(3);
        List<Task> history = historyManager.getHistory();

        assertEquals(List.of(t1, t2), history);
    }

}