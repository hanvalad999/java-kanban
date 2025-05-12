//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

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
        Assertions.assertEquals(testTask, this.manager.getTaskById(testTask.getId()));
    }

    @Test
    void testSubtaskEqualityById() {
        Epic testEpic = new Epic("Test epic", "Description", 14);
        this.manager.createEpic(testEpic);
        Subtask subtask = new Subtask("Test subtask", "Description", 15, Status.NEW, testEpic.getId());
        this.manager.createSubtask(subtask);
        Assertions.assertEquals(subtask, this.manager.getSubtaskById(subtask.getId()));
    }

    @Test
    void testEpicEqualityById() {
        Epic testEpic = new Epic("Test epic1", "Description1", 16);
        this.manager.createEpic(testEpic);
        Assertions.assertEquals(testEpic, this.manager.getEpicById(testEpic.getId()));
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
        Assertions.assertEquals(checkTask, this.manager.getHistory().getFirst());
    }

    @Test
    void addNewTask() {
        Task task = new Task("Test task", "Description", 20, Status.NEW);
        this.manager.createTask(task);
        Task savedTask = this.manager.getTaskById(task.getId());
        Assertions.assertNotNull(savedTask, "Задача не найдена.");
        Assertions.assertEquals(task, savedTask, "Задачи не совпадают.");
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
        Assertions.assertEquals(subtask, savedSubtask);
    }

    @Test
    void deleteAllTasks() {
        this.manager.clearAllTasks();
        List<Task> tasks = this.manager.getAllTasks();
        Assertions.assertEquals(0, tasks.size());
    }

    @Test
    void deleteAllEpics() {
        this.manager.clearAllEpics();
        List<Epic> epics = this.manager.getAllEpics();
        Assertions.assertEquals(0, epics.size());
    }

    @Test
    void deleteAllSubtasks() {
        this.manager.clearAllSubtasks();
        List<Subtask> subtasks = this.manager.getAllSubtasks();
        Assertions.assertEquals(0, subtasks.size());
    }
}
