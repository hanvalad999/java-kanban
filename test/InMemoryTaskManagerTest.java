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

public class InMemoryTaskManagerTest {
    private TaskManager taskManager;

    public InMemoryTaskManagerTest() {
    }

    @BeforeEach
    void setUp() {
        this.taskManager = Managers.getDefault();
    }

    @Test
    void shouldInitializeTaskManager() {
        Assertions.assertNotNull(this.taskManager, "Менеджер задач должен быть проинициализирован");
    }

    @Test
    void shouldCreateAndFindTask() {
        Task task = new Task("Test task", "Description", 1, Status.NEW);
        Task createdTask = this.taskManager.createTask(task);
        Task foundTask = this.taskManager.getTaskById(createdTask.getId());
        Assertions.assertNotNull(foundTask, "Задача должна быть найдена");
        Assertions.assertEquals(task.getTitle(), foundTask.getTitle(), "Названия задач должны совпадать");
    }

    @Test
    void shouldCreateEpicWithSubtasks() {
        Epic epic = new Epic("Test epic", "Description", 1);
        Epic createdEpic = this.taskManager.createEpic(epic);
        Subtask subtask = new Subtask("Test subtask", "Description", 1, Status.NEW, createdEpic.getId());
        Subtask createdSubtask = this.taskManager.createSubtask(subtask);
        List<Subtask> epicSubtasks = this.taskManager.getSubtasksOfEpic(createdEpic.getId());
        Assertions.assertEquals(1, epicSubtasks.size(), "У эпика должна быть 1 подзадача");
        Assertions.assertEquals(createdSubtask.getId(), ((Subtask)epicSubtasks.get(0)).getId(), "ID подзадач должны совпадать");
    }

    @Test
    void shouldUpdateTaskStatus() {
        Task task = new Task("Test task", "Description", 2, Status.NEW);
        Task createdTask = this.taskManager.createTask(task);
        createdTask.setStatus(Status.IN_PROGRESS);
        this.taskManager.updateTask(createdTask);
        Task updatedTask = this.taskManager.getTaskById(createdTask.getId());
        Assertions.assertEquals(Status.IN_PROGRESS, updatedTask.getStatus(), "Статус должен быть обновлен");
    }
}
