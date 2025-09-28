package test;

import manager.TaskManager;
import model.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import manager.TimeIntersectionException;

import static org.junit.jupiter.api.Assertions.*;

public abstract class AbstractTaskManagerTest<T extends TaskManager> {

    protected T manager;

    protected abstract T createManager();

    @BeforeEach
    void setUp() {
        manager = createManager();
    }

    protected Task newTask(String name) {
        return new Task(name, "desc:" + name, 0, Status.NEW);
    }

    protected Epic newEpic(String name) {
        return new Epic(name, "desc:" + name, 0);
    }

    protected Subtask newSub(Epic epic, String name, Status st) {
        return new Subtask(name, "desc:" + name, 0, st, epic.getId());
    }

    @Test
    void taskNotEqual() {
        Task t1 = new Task("A", "D", 7, Status.NEW);
        Task t2 = new Task("B", "D", 8, Status.NEW);
        assertNotEquals(t1, t2);
    }

    @Test
    void testNotEqualsSubtask() {
        Subtask s1 = new Subtask("S1", "D", 9, Status.NEW, 1);
        Subtask s2 = new Subtask("S2", "D", 10, Status.NEW, 2);
        assertNotEquals(s1, s2);
    }

    @Test
    void testNotEqualsEpic() {
        Epic e1 = new Epic("E1", "D", 11);
        Epic e2 = new Epic("E2", "D", 12);
        assertNotEquals(e1, e2);
    }

    @Test
    void testTasksEqualityById() {
        Task t = newTask("T");
        t = manager.createTask(t);
        assertEquals(t, manager.getTaskById(t.getId()));
    }

    @Test
    void testSubtaskEqualityById() {
        Epic e = manager.createEpic(newEpic("E"));
        Subtask s = manager.createSubtask(newSub(e, "S", Status.NEW));
        assertEquals(s, manager.getSubtaskById(s.getId()));
    }

    @Test
    void testEpicEqualityById() {
        Epic e = manager.createEpic(newEpic("E"));
        assertEquals(e, manager.getEpicById(e.getId()));
    }

    @Test
    void addNewTask_andGetAll() {
        Task t = manager.createTask(newTask("T"));
        Task got = manager.getTaskById(t.getId());
        assertNotNull(got);
        assertEquals(t, got);
        List<Task> all = manager.getAllTasks();
        assertTrue(all.contains(t));
    }

    @Test
    void clearAllTasks() {
        manager.createTask(newTask("A"));
        manager.createTask(newTask("B"));
        manager.clearAllTasks();
        assertTrue(manager.getAllTasks().isEmpty());
    }

    @Test
    void clearAllEpicsAlsoClearsSubtasks() {
        Epic e = manager.createEpic(newEpic("E"));
        manager.createSubtask(newSub(e, "S1", Status.NEW));
        manager.createSubtask(newSub(e, "S2", Status.NEW));
        manager.clearAllEpics();
        assertTrue(manager.getAllEpics().isEmpty());
        assertTrue(manager.getAllSubtasks().isEmpty());
    }

    @Test
    void taskRemovedFromHistoryWhenDeleted() {
        Task t = manager.createTask(newTask("T"));
        manager.getTaskById(t.getId());
        assertTrue(manager.getHistory().contains(t));
        manager.deleteTask(t.getId());
        assertFalse(manager.getHistory().contains(t));
    }

    @Test
    void epicStatus_allNEW() {
        Epic e = manager.createEpic(newEpic("E"));
        manager.createSubtask(newSub(e, "S1", Status.NEW));
        manager.createSubtask(newSub(e, "S2", Status.NEW));
        assertEquals(Status.NEW, manager.getEpicById(e.getId()).getStatus());
    }

    @Test
    void epicStatus_allDONE() {
        Epic e = manager.createEpic(newEpic("E"));
        manager.createSubtask(newSub(e, "S1", Status.DONE));
        manager.createSubtask(newSub(e, "S2", Status.DONE));
        assertEquals(Status.DONE, manager.getEpicById(e.getId()).getStatus());
    }

    @Test
    void epicStatus_mixNEWandDONE() {
        Epic e = manager.createEpic(newEpic("E"));
        manager.createSubtask(newSub(e, "S1", Status.NEW));
        manager.createSubtask(newSub(e, "S2", Status.DONE));
        assertEquals(Status.IN_PROGRESS, manager.getEpicById(e.getId()).getStatus());
    }

    @Test
    void epicStatus_hasIN_PROGRESS() {
        Epic e = manager.createEpic(newEpic("E"));
        manager.createSubtask(newSub(e, "S1", Status.IN_PROGRESS));
        manager.createSubtask(newSub(e, "S2", Status.NEW));
        assertEquals(Status.IN_PROGRESS, manager.getEpicById(e.getId()).getStatus());
    }

    @Test
    void overlappingIntervals_throw() {
        LocalDateTime base = LocalDateTime.now().withSecond(0).withNano(0);

        Task a = newTask("A");
        a.setStartTime(base);
        a.setDuration(Duration.ofMinutes(60)); // [t .. t+60)
        manager.createTask(a);

        Task b = newTask("B");
        b.setStartTime(base.plusMinutes(30));  // [t+30 .. t+90)
        b.setDuration(Duration.ofMinutes(60));

        assertThrows(TimeIntersectionException.class, () -> manager.createTask(b));
    }

    @Test
    void touchingIntervals_allowed() {
        LocalDateTime base = LocalDateTime.now().withSecond(0).withNano(0);

        Task a = newTask("A");
        a.setStartTime(base);
        a.setDuration(Duration.ofMinutes(60)); // [t .. t+60)
        manager.createTask(a);

        Task b = newTask("B");
        b.setStartTime(base.plusMinutes(60));  // [t+60 .. t+120)
        b.setDuration(Duration.ofMinutes(60));

        assertDoesNotThrow(() -> manager.createTask(b));
    }

    @Test
    void prioritizedTasks_sortedByStart_excludesNullStart() {
        LocalDateTime base = LocalDateTime.now().withSecond(0).withNano(0);

        Task later = newTask("LATER");
        later.setStartTime(base.plusHours(2));
        later.setDuration(Duration.ofMinutes(30));
        manager.createTask(later);

        Task sooner = newTask("SOONER");
        sooner.setStartTime(base.plusHours(1));
        sooner.setDuration(Duration.ofMinutes(30));
        manager.createTask(sooner);

        Task noTime = newTask("NO_TIME"); // без времени
        manager.createTask(noTime);

        List<Task> prio = manager.getPrioritizedTasks();
        assertEquals(2, prio.size());
        assertEquals(sooner.getId(), prio.get(0).getId());
        assertEquals(later.getId(), prio.get(1).getId());
    }

    @Test
    void epicTimingAggregatesSubtasks() {
        Epic e = manager.createEpic(newEpic("E"));

        LocalDateTime s1 = LocalDateTime.now().withSecond(0).withNano(0);
        LocalDateTime s2 = s1.plusHours(2);

        Subtask sub1 = newSub(e, "S1", Status.NEW);
        sub1.setStartTime(s1);
        sub1.setDuration(Duration.ofMinutes(90)); // end s1+90
        manager.createSubtask(sub1);

        Subtask sub2 = newSub(e, "S2", Status.NEW);
        sub2.setStartTime(s2);
        sub2.setDuration(Duration.ofMinutes(30)); // end s2+30
        manager.createSubtask(sub2);

        Epic got = manager.getEpicById(e.getId());
        assertEquals(Duration.ofMinutes(120), got.getDuration());
        assertEquals(s1, got.getStartTime());
        assertEquals(s2.plusMinutes(30), got.getEndTime());
    }
}
