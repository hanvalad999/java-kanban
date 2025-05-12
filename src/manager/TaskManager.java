package manager;

import model.Epic;
import model.Task;
import model.Subtask;

import java.util.List;

public interface TaskManager {
    public Task createTask(Task task);
    public Epic createEpic(Epic epic);
    public Subtask createSubtask(Subtask subtask);

    public List<Task> getAllTasks();
    public List<Epic> getAllEpics();
    public List<Subtask> getAllSubtasks();

    public Task getTaskById(int id);
    public Epic getEpicById(int id);
    public Subtask getSubtaskById(int id);

    public void updateTask(Task updatedTask);
    public void updateEpic(Epic updatedEpic);
    public void updateSubtask(Subtask updatedSubtask);

    public void deleteTask(int id);
    public void deleteEpic(int id);
    public void deleteSubtask(int id);

    public void clearAllTasks();
    public void clearAllEpics();
    public void clearAllSubtasks();

    public List<Subtask> getSubtasksOfEpic(int epicId);

    public List<Task> getHistory();

}
