package manager;

import model.Task; // не знал если находятся в разных пакетов
import model.Epic;  // , классы task и т.д не видят друг д/руга пришлось не много подумать
import model.Subtask;
import model.Status;

import java.util.*;

public class TaskManager {
    private int nextId = 1;

    private final Map<Integer, Task> tasks = new HashMap<>();
    private final Map<Integer, Epic> epics = new HashMap<>();
    private final Map<Integer, Subtask> subtasks = new HashMap<>();

    // Создание задач
    public Task createTask(Task task) {
        task.setId(nextId++);
        tasks.put(task.getId(), task);
        return task;
    }

    public Epic createEpic(Epic epic) {
        epic.setId(nextId++);
        epics.put(epic.getId(), epic);
        return epic;
    }

    public Subtask createSubtask(Subtask subtask) {
        subtask.setId(nextId++);
        subtasks.put(subtask.getId(), subtask);
        Epic epic = epics.get(subtask.getEpicId());
        if (epic != null) {
            epic.addSubtask(subtask.getId());
            updateEpicStatus(epic);
        }
        return subtask;
    }

    // Получение всех задач
    public List<Task> getAllTasks() {
        return new ArrayList<>(tasks.values());
    }

    public List<Epic> getAllEpics() {
        return new ArrayList<>(epics.values());
    }

    public List<Subtask> getAllSubtasks() {
        return new ArrayList<>(subtasks.values());
    }

    // Получение по ID
    public Task getTaskById(int id) {
        return tasks.get(id);
    }

    public Epic getEpicById(int id) {
        return epics.get(id);
    }

    public Subtask getSubtaskById(int id) {
        return subtasks.get(id);
    }

    // Обновление
    public void updateTask(Task updatedTask) {
        tasks.put(updatedTask.getId(), updatedTask);
    }

    public void updateEpic(Epic updatedEpic) {
        epics.put(updatedEpic.getId(), updatedEpic);
        updateEpicStatus(updatedEpic);
    }

    public void updateSubtask(Subtask updatedSubtask) {
        subtasks.put(updatedSubtask.getId(), updatedSubtask);
        Epic epic = epics.get(updatedSubtask.getEpicId());
        if (epic != null) {
            updateEpicStatus(epic);
        }
    }

    // Удаление
    public void deleteTask(int id) {
        tasks.remove(id);
    }

    public void deleteEpic(int id) {
        Epic epic = epics.remove(id);
        if (epic != null) {
            for (Integer subId : epic.getSubtaskIds()) {
                subtasks.remove(subId);
            }
        }
    }

    public void deleteSubtask(int id) {
        Subtask subtask = subtasks.remove(id);
        if (subtask != null) {
            Epic epic = epics.get(subtask.getEpicId());
            if (epic != null) {
                epic.removeSubtask(id);
                updateEpicStatus(epic);
            }
        }
    }

    // Очистка
    public void clearAllTasks() {
        tasks.clear();
    }

    public void clearAllEpics() {
        for (Epic epic : epics.values()) {
            for (Integer subId : epic.getSubtaskIds()) {
                subtasks.remove(subId);
            }
        }
        epics.clear();
    }

    public void clearAllSubtasks() {
        for (Subtask sub : subtasks.values()) {
            Epic epic = epics.get(sub.getEpicId());
            if (epic != null) {
                epic.clearSubtasks();
                updateEpicStatus(epic);
            }
        }
        subtasks.clear();
    }

    // Подзадачи эпика
    public List<Subtask> getSubtasksOfEpic(int epicId) {
        List<Subtask> list = new ArrayList<>();
        Epic epic = epics.get(epicId);
        if (epic != null) {
            for (Integer id : epic.getSubtaskIds()) {
                list.add(subtasks.get(id));
            }
        }
        return list;
    }

    // Логика статуса эпика
    private void updateEpicStatus(Epic epic) {
        List<Integer> subtaskIds = epic.getSubtaskIds();
        if (subtaskIds.isEmpty()) {
            epic.setStatus(Status.NEW);
            return;
        }
        // HashSet мы пока что не проходили,но спасибо что то новое узнал
        Set<Status> uniqueStatuses = new HashSet<>();
        for (int id : subtaskIds) {
            uniqueStatuses.add(subtasks.get(id).getStatus());
        }

        if (uniqueStatuses.size() == 1) {
            epic.setStatus(uniqueStatuses.iterator().next());
        } else {
            epic.setStatus(Status.IN_PROGRESS);
        }
    }
}