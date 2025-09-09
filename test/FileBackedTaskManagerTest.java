package test;

import manager.FileBackedTaskManager;

import java.io.IOException;
import java.util.List;
import java.nio.file.Files;
import java.io.File;
import java.nio.file.Path;
import model.*;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

public class FileBackedTaskManagerTest {

    @Test
    void shouldSaveAndLoadEmptyFile() throws IOException {
        File file = File.createTempFile("task", ".csv");
        FileBackedTaskManager manager1 = new FileBackedTaskManager(file);

        manager1.save();

        FileBackedTaskManager manager2 = FileBackedTaskManager.loadFromFile(file);

        assertEquals(0, manager2.getAllTasks().size());
        assertEquals(0, manager2.getAllEpics().size());
        assertEquals(0, manager2.getAllSubtasks().size());
    }


    @Test
    void shouldSaveSeveralTasks() throws IOException {
        File file = File.createTempFile("task", ".csv");
        FileBackedTaskManager manager1 = new FileBackedTaskManager(file);

        Task task1 = new Task("Task1", "Description1", 1, Status.NEW);
        manager1.createTask(task1);

        Epic epic1 = new Epic("Epic1", "DescriptionEpic", 2);
        manager1.createEpic(epic1);

        Subtask subtask1 = new Subtask("Sub1", "DescriptionSub", 3, Status.DONE, epic1.getId());
        manager1.createSubtask(subtask1);

        manager1.save();

        List<String> lines = Files.readAllLines(file.toPath());

        // 6. Проверяем содержимое без stream
        boolean hasTask = false;
        boolean hasEpic = false;
        boolean hasSubtask = false;

        for (String line : lines) {
            if (line.contains("TASK")) {
                hasTask = true;
            }
            if (line.contains("EPIC")) {
                hasEpic = true;
            }
            if (line.contains("SUBTASK")) {
                hasSubtask = true;
            }
        }

        assertTrue(hasTask, "Файл должен содержать строку с TASK");
        assertTrue(hasEpic, "Файл должен содержать строку с EPIC");
        assertTrue(hasSubtask, "Файл должен содержать строку с SUBTASK");
    }


    @Test
    void shouldLoadSeveralTasksFromFile() throws IOException {
        File file = File.createTempFile("task", ".csv");
        FileBackedTaskManager manager1 = new FileBackedTaskManager(file);

        Task task1 = new Task("Task1", "Description1", 1, Status.NEW);
        manager1.createTask(task1);

        Epic epic1 = new Epic("Epic1", "DescriptionEpic", 2);
        manager1.createEpic(epic1);

        Subtask subtask1 = new Subtask("Sub1", "DescriptionSub", 3, Status.DONE, epic1.getId());
        manager1.createSubtask(subtask1);

        manager1.save();

        FileBackedTaskManager manager2 = FileBackedTaskManager.loadFromFile(file);

        assertEquals(1, manager2.getAllTasks().size(), "Должна быть 1 обычная задача");
        assertEquals(1, manager2.getAllEpics().size(), "Должен быть 1 эпик");
        assertEquals(1, manager2.getAllSubtasks().size(), "Должна быть 1 подзадача");

        Subtask loadedSub = manager2.getAllSubtasks().get(0);
        assertEquals(epic1.getId(), loadedSub.getEpicId(), "У подзадачи должен быть правильный epicId");

        Epic loadedEpic = manager2.getAllEpics().get(0);
        assertTrue(loadedEpic.getSubtaskIds().contains(loadedSub.getId()),
                "Эпик должен содержать id своей подзадачи");
    }

}