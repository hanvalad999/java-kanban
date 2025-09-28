package test;

import java.util.List;
import manager.Managers;
import manager.TaskManager;
import model.Epic;
import model.Status;
import model.Subtask;
import model.Task;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDateTime;
import manager.TimeIntersectionException;

import static org.junit.jupiter.api.Assertions.*;

class TaskManagerTest {
    private TaskManager manager;

    TaskManagerTest() {
    }

    @BeforeEach
    void beforeEach() {
        this.manager = Managers.getDefault();
        Task task1 = new Task("Test task", "Description", 1, Status.NEW);
        Task task2 = new Task("Test task1", "Description1", 2, Status.NEW);
        this.manager.createTask(task1);
        this.manager.createTask(task2);
        Epic epic1 = new Epic("Test epic1", "Description1", 3);
        Epic epic2 = new Epic("Test epic2", "Description2", 4);
        this.manager.createEpic(epic1);
        this.manager.createEpic(epic2);
        Subtask subtask1 = new Subtask("Test subtask1", "Description1", 5, Status.NEW, epic1.getId());
        Subtask subtask2 = new Subtask("Test subtask2", "Description2", 6, Status.NEW, epic1.getId());
        this.manager.createSubtask(subtask1);
        this.manager.createSubtask(subtask2);
    }

    @Test
    void taskNotEqual() {
        Task task1 = new Task("Test task3", "Description3", 7, Status.NEW);
        Task task2 = new Task("Test task4", "Description4", 8, Status.NEW);
        Assertions.assertNotEquals(task1, task2);
    }

    @Test
    void testNotEqualsSubtask() {
        Subtask subtask1 = new Subtask("Test subtask1", "Description1", 9, Status.NEW, 1);
        Subtask subtask2 = new Subtask("Test subtask2", "Description2", 10, Status.NEW, 2);
        Assertions.assertNotEquals(subtask1, subtask2);
    }

    @Test
    void testNotEqualsEpic() {
        Epic epic1 = new Epic("Test epic1", "Description1", 11);
        Epic epic2 = new Epic("Test epic2", "Description2", 12);
        Assertions.assertNotEquals(epic1, epic2);
    }

    @Test
    void testTasksEqualityById() {
        Task testTask = new Task("Test epic1", "Description1", 13, Status.NEW);
        this.manager.createTask(testTask);
        assertEquals(testTask, this.manager.getTaskById(testTask.getId()));
    }

    @Test
    void testSubtaskEqualityById() {
        Epic testEpic = new Epic("Test epic", "Description", 14);
        this.manager.createEpic(testEpic);
        Subtask subtask = new Subtask("Test subtask", "Description", 15, Status.NEW, testEpic.getId());
        this.manager.createSubtask(subtask);
        assertEquals(subtask, this.manager.getSubtaskById(subtask.getId()));
    }

    @Test
    void testEpicEqualityById() {
        Epic testEpic = new Epic("Test epic1", "Description1", 16);
        this.manager.createEpic(testEpic);
        assertEquals(testEpic, this.manager.getEpicById(testEpic.getId()));
    }

    @Test
    void checkForIdConflicts() {
        Task testTask1 = new Task("Test task", "Description", 17, Status.NEW);
        Task testTask2 = new Task("Test task1", "Description1", 18, Status.NEW);
        this.manager.createTask(testTask1);
        this.manager.createTask(testTask2);
        Assertions.assertNotEquals(this.manager.getTaskById(testTask1.getId()), this.manager.getTaskById(testTask2.getId()));
    }

    @Test
    void checkHistoryManagerSavesTaskVersions() {
        Task checkTask = new Task("Test task", "Description", 19, Status.NEW);
        this.manager.createTask(checkTask);
        this.manager.getTaskById(checkTask.getId());
        Task updatedTask = new Task("Test task", "Description", checkTask.getId(), Status.IN_PROGRESS);
        this.manager.updateTask(updatedTask);
        this.manager.getTaskById(updatedTask.getId());
        assertEquals(checkTask, this.manager.getHistory().get(0));
    }

    @Test
    void addNewTask() {
        Task task = new Task("Test task", "Description", 20, Status.NEW);
        this.manager.createTask(task);
        Task savedTask = this.manager.getTaskById(task.getId());
        Assertions.assertNotNull(savedTask, "Задача не найдена.");
        assertEquals(task, savedTask, "Задачи не совпадают.");
        List<Task> tasks = this.manager.getAllTasks();
        Assertions.assertNotNull(tasks, "Задачи не возвращаются.");
        Assertions.assertTrue(tasks.contains(task), "Задача не найдена в списке.");
    }

    @Test
    void addNewSubtask() {
        Epic epic = new Epic("Test epic1", "Description1", 21);
        this.manager.createEpic(epic);
        Subtask subtask = new Subtask("Test subtask", "Desc", 22, Status.NEW, epic.getId());
        this.manager.createSubtask(subtask);
        Subtask savedSubtask = this.manager.getSubtaskById(subtask.getId());
        Assertions.assertNotNull(savedSubtask);
        assertEquals(subtask, savedSubtask);
    }

    @Test
    void deleteAllTasks() {
        this.manager.clearAllTasks();
        List<Task> tasks = this.manager.getAllTasks();
        assertEquals(0, tasks.size());
    }

    @Test
    void deleteAllEpics() {
        this.manager.clearAllEpics();
        List<Epic> epics = this.manager.getAllEpics();
        assertEquals(0, epics.size());
    }

    @Test
    void deleteAllSubtasks() {
        this.manager.clearAllSubtasks();
        List<Subtask> subtasks = this.manager.getAllSubtasks();
        assertEquals(0, subtasks.size());
    }

    @Test
    void taskShouldBeRemovedFromHistoryWhenDeleted() {
        Task task = new Task("Test task", "Description", 20, Status.NEW);
        manager.createTask(task);

        manager.getTaskById(task.getId());
        Assertions.assertTrue(manager.getHistory().contains(task), "Задача должна быть в истории");

        manager.deleteTask(task.getId());
        Assertions.assertFalse(manager.getHistory().contains(task), "Задача не была удалена из истории");
    }

    // Новые Тесты:

    // Статус эпика: все NEW
    @Test
    void epicStatus_allNew() {
        Epic e = manager.createEpic(new Epic("E", "ED", 0));
        manager.createSubtask(new Subtask("S1", "SD1", 0, Status.NEW, e.getId()));
        manager.createSubtask(new Subtask("S2", "SD2", 0, Status.NEW, e.getId()));
        assertEquals(Status.NEW, manager.getEpicById(e.getId()).getStatus());
    }

    // Статус эпика: все DONE
    @Test
    void epicStatus_allDone() {
        Epic e = manager.createEpic(new Epic("E", "ED", 0));
        manager.createSubtask(new Subtask("S1", "SD1", 0, Status.DONE, e.getId()));
        manager.createSubtask(new Subtask("S2", "SD2", 0, Status.DONE, e.getId()));
        assertEquals(Status.DONE, manager.getEpicById(e.getId()).getStatus());
    }

    // Статус эпика: NEW + DONE -> IN_PROGRESS
    @Test
    void epicStatus_mixNewDone() {
        Epic e = manager.createEpic(new Epic("E", "ED", 0));
        manager.createSubtask(new Subtask("S1", "SD1", 0, Status.NEW, e.getId()));
        manager.createSubtask(new Subtask("S2", "SD2", 0, Status.DONE, e.getId()));
        assertEquals(Status.IN_PROGRESS, manager.getEpicById(e.getId()).getStatus());
    }

    // Статус эпика: есть IN_PROGRESS -> IN_PROGRESS
    @Test
    void epicStatus_inProgressPresent() {
        Epic e = manager.createEpic(new Epic("E", "ED", 0));
        manager.createSubtask(new Subtask("S1", "SD1", 0, Status.IN_PROGRESS, e.getId()));
        manager.createSubtask(new Subtask("S2", "SD2", 0, Status.NEW, e.getId()));
        assertEquals(Status.IN_PROGRESS, manager.getEpicById(e.getId()).getStatus());
    }

    // сортировка по startTime + исключение задач без startTime
    @Test
    void prioritizedTasks_sortedByStart_excludesNullStart() {
        LocalDateTime base = LocalDateTime.now().withSecond(0).withNano(0);

        Task t1 = new Task("PR1", "D", 0, Status.NEW);
        t1.setStartTime(base.plusHours(2));
        t1.setDuration(Duration.ofMinutes(30));
        manager.createTask(t1);

        Task t2 = new Task("PR2", "D", 0, Status.NEW);
        t2.setStartTime(base.plusHours(1));
        t2.setDuration(Duration.ofMinutes(30));
        manager.createTask(t2);

        Task noTime = new Task("PR3", "D", 0, Status.NEW); // без времени
        manager.createTask(noTime);

        List<Task> prio = manager.getPrioritizedTasks();
        assertEquals(2, prio.size());
        assertEquals(t2.getId(), prio.get(0).getId());
        assertEquals(t1.getId(), prio.get(1).getId());
    }

    @Test
    void intersection_overlapping_throws() {
        LocalDateTime base = LocalDateTime.now().withSecond(0).withNano(0);

        Task a = new Task("A", "D", 0, Status.NEW);
        a.setStartTime(base);
        a.setDuration(Duration.ofMinutes(60)); // [t .. t+60)
        manager.createTask(a);

        Task b = new Task("B", "D", 0, Status.NEW);
        b.setStartTime(base.plusMinutes(30));  // [t+30 .. t+90)
        b.setDuration(Duration.ofMinutes(60));

        assertThrows(TimeIntersectionException.class, () -> manager.createTask(b));
    }

    @Test
    void intersection_touching_allowed() {
        LocalDateTime base = LocalDateTime.now().withSecond(0).withNano(0);

        Task a = new Task("A", "D", 0, Status.NEW);
        a.setStartTime(base);
        a.setDuration(Duration.ofMinutes(60)); // [t .. t+60)
        manager.createTask(a);

        Task b = new Task("B", "D", 0, Status.NEW);
        b.setStartTime(base.plusMinutes(60));  // [t+60 .. t+120)
        b.setDuration(Duration.ofMinutes(60));

        assertDoesNotThrow(() -> manager.createTask(b));
    }

    @Test
    void epicTime_aggregatesFromSubtasks() {
        Epic e = manager.createEpic(new Epic("Etime", "ED", 0));

        LocalDateTime s1Start = LocalDateTime.now().withSecond(0).withNano(0);
        LocalDateTime s2Start = s1Start.plusHours(2);

        Subtask s1 = new Subtask("S1", "SD1", 0, Status.NEW, e.getId());
        s1.setStartTime(s1Start);
        s1.setDuration(Duration.ofMinutes(90)); // end t+90
        manager.createSubtask(s1);

        Subtask s2 = new Subtask("S2", "SD2", 0, Status.NEW, e.getId());
        s2.setStartTime(s2Start);
        s2.setDuration(Duration.ofMinutes(30)); // end t+150
        manager.createSubtask(s2);

        Epic actual = manager.getEpicById(e.getId());
        assertEquals(Duration.ofMinutes(120), actual.getDuration());
        assertEquals(s1Start, actual.getStartTime());
        assertEquals(s2Start.plusMinutes(30), actual.getEndTime());
    }
}
