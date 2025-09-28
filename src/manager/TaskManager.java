package manager;

import model.Epic;
import model.Task;
import model.Subtask;

import java.util.List;

public interface TaskManager {

    Task createTask(Task task) throws TimeIntersectionException;

    Epic createEpic(Epic epic);

    Subtask createSubtask(Subtask subtask) throws TimeIntersectionException;

    List<Task> getAllTasks();

    List<Epic> getAllEpics();

    List<Subtask> getAllSubtasks();

    Task getTaskById(int id);

    Epic getEpicById(int id);

    Subtask getSubtaskById(int id);

    void updateTask(Task updatedTask) throws TimeIntersectionException;

    void updateEpic(Epic updatedEpic);

    void updateSubtask(Subtask updatedSubtask) throws  TimeIntersectionException;

    void deleteTask(int id);

    void deleteEpic(int id);

    void deleteSubtask(int id);

    void clearAllTasks();

    void clearAllEpics();

    void clearAllSubtasks();

    List<Subtask> getSubtasksOfEpic(int epicId);

    List<Task> getHistory();

    List<Task> getPrioritizedTasks();
}
