package manager;

import model.Task;
import model.Epic;
import model.Subtask;
import model.Status;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class InMemoryTaskManager implements TaskManager {
    private int nextId = 1; // счётчик для генерации ID

    // Основные коллекции задач
    private final Map<Integer, Task> tasks = new HashMap<>();
    private final Map<Integer, Epic> epics = new HashMap<>();
    private final Map<Integer, Subtask> subtasks = new HashMap<>();

    private final HistoryManager historyManager;

    // Множество задач в порядке приоритета (по startTime)
    private final Set<Task> prioritizedTasks = new TreeSet<>((a, b) -> {
        if (a == b) return 0;
        var sa = a.getStartTime();
        var sb = b.getStartTime();
        if (sa == null && sb == null) return Integer.compare(a.getId(), b.getId());
        if (sa == null) return 1;
        if (sb == null) return -1;
        int cmp = sa.compareTo(sb);
        return (cmp != 0) ? cmp : Integer.compare(a.getId(), b.getId());
    });

    public InMemoryTaskManager(HistoryManager historyManager) {
        this.historyManager = historyManager;
    }

    // === СОЗДАНИЕ ЗАДАЧ ===

    @Override
    public Task createTask(Task task) throws TimeIntersectionException {
        task.setId(nextId++);
        if (hasIntersection(task)) {
            throw new TimeIntersectionException("Пересечение по времени задач");
        }
        tasks.put(task.getId(), task);
        addToPrioritized(task);
        return task;
    }

    @Override
    public Epic createEpic(Epic epic) {
        epic.setId(nextId++);
        epics.put(epic.getId(), epic);
        return epic;
    }

    @Override
    public Subtask createSubtask(Subtask sub) throws TimeIntersectionException {
        sub.setId(nextId++);
        if (hasIntersection(sub)) {
            throw new TimeIntersectionException("Пересечение по времени задач/подзадач");
        }
        subtasks.put(sub.getId(), sub);
        addToPrioritized(sub);

        // Привязка подзадачи к эпику
        Epic epic = epics.get(sub.getEpicId());
        if (epic != null) {
            epic.addSubtask(sub.getId());
            updateEpicStatus(epic);
            updateEpicTime(epic);
        }
        return sub;
    }

    // === ПОЛУЧЕНИЕ ВСЕХ ЗАДАЧ ===

    @Override
    public List<Task> getAllTasks() {
        return new ArrayList<>(tasks.values());
    }

    @Override
    public List<Epic> getAllEpics() {
        return new ArrayList<>(epics.values());
    }

    @Override
    public List<Subtask> getAllSubtasks() {
        return new ArrayList<>(subtasks.values());
    }

    @Override
    public List<Task> getHistory() {
        return historyManager.getHistory();
    }

    // === ПОЛУЧЕНИЕ ПО ID ===

    @Override
    public Task getTaskById(int id) {
        Task task = tasks.get(id);
        if (task == null) throw new NotFoundException("Task id=" + id + " не найден");
        historyManager.add(task);
        return task;
    }

    @Override
    public Epic getEpicById(int id) {
        Epic epic = epics.get(id);
        if (epic == null) throw new NotFoundException("Epic id=" + id + " не найден");
        historyManager.add(epic);
        return epic;
    }

    @Override
    public Subtask getSubtaskById(int id) {
        Subtask subtask = subtasks.get(id);
        if (subtask == null) throw new NotFoundException("Subtask id=" + id + " не найден");
        historyManager.add(subtask);
        return subtask;
    }

    // === ОБНОВЛЕНИЕ ===

    @Override
    public void updateTask(Task updated) throws TimeIntersectionException {
        int id = updated.getId();
        if (!tasks.containsKey(id))
            throw new NotFoundException("Task id=" + id + " не найден");

        Task old = tasks.get(id);
        removeFromPrioritized(old);
        if (hasIntersection(updated)) {
            addToPrioritized(old);
            throw new TimeIntersectionException("Пересечение по времени задач");
        }
        tasks.put(id, updated);
        addToPrioritized(updated);
    }

    @Override
    public void updateEpic(Epic updatedEpic) {
        int id = updatedEpic.getId();
        if (!epics.containsKey(id))
            throw new NotFoundException("Epic id=" + id + " не найден");

        epics.put(id, updatedEpic);
        updateEpicStatus(updatedEpic);
        updateEpicTime(updatedEpic);
    }

    @Override
    public void updateSubtask(Subtask updatedSubtask) throws TimeIntersectionException {
        int id = updatedSubtask.getId();
        if (!subtasks.containsKey(id))
            throw new NotFoundException("Subtask id=" + id + " не найдена");

        Subtask old = subtasks.get(id);
        removeFromPrioritized(old);
        if (hasIntersection(updatedSubtask)) {
            addToPrioritized(old);
            throw new TimeIntersectionException("Пересечение по времени подзадач");
        }
        subtasks.put(id, updatedSubtask);
        addToPrioritized(updatedSubtask);

        // обновляем связанный эпик
        Epic epic = epics.get(updatedSubtask.getEpicId());
        if (epic != null) {
            updateEpicStatus(epic);
            updateEpicTime(epic);
        }
    }

    // === УДАЛЕНИЕ ===

    @Override
    public void deleteTask(int id) {
        if (!tasks.containsKey(id))
            throw new NotFoundException("Task id=" + id + " не найден");

        Task removed = tasks.remove(id);
        removeFromPrioritized(removed);
        historyManager.remove(id);
    }

    @Override
    public void deleteEpic(int id) {
        if (!epics.containsKey(id))
            throw new NotFoundException("Epic id=" + id + " не найден");

        Epic epic = epics.remove(id);
        for (Integer subId : epic.getSubtaskIds()) {
            Subtask removed = subtasks.remove(subId);
            removeFromPrioritized(removed);
            historyManager.remove(subId);
        }
        historyManager.remove(id);
    }

    @Override
    public void deleteSubtask(int id) {
        if (!subtasks.containsKey(id))
            throw new NotFoundException("Subtask id=" + id + " не найдена");

        Subtask removed = subtasks.remove(id);
        removeFromPrioritized(removed);

        Epic epic = epics.get(removed.getEpicId());
        if (epic != null) {
            epic.removeSubtask(id);
            updateEpicStatus(epic);
            updateEpicTime(epic);
        }
        historyManager.remove(id);
    }

    // === ОЧИСТКА ===

    @Override
    public void clearAllTasks() {
        tasks.values().forEach(this::removeFromPrioritized);
        tasks.clear();
    }

    @Override
    public void clearAllEpics() {
        for (Epic epic : epics.values()) {
            for (Integer subId : epic.getSubtaskIds()) {
                Subtask removed = subtasks.remove(subId);
                removeFromPrioritized(removed);
                historyManager.remove(subId);
            }
        }
        epics.clear();
    }

    @Override
    public void clearAllSubtasks() {
        for (Subtask sub : subtasks.values()) {
            removeFromPrioritized(sub);
            Epic epic = epics.get(sub.getEpicId());
            if (epic != null) {
                epic.clearSubtasks();
                updateEpicStatus(epic);
                updateEpicTime(epic);
            }
        }
        subtasks.clear();
    }

    // === ПОДЗАДАЧИ ЭПИКА ===

    @Override
    public List<Subtask> getSubtasksOfEpic(int epicId) {
        Epic epic = epics.get(epicId);
        if (epic == null) throw new NotFoundException("Epic id=" + epicId + " не найден");
        return epic.getSubtaskIds().stream()
                .map(subtasks::get)
                .filter(Objects::nonNull)
                .toList();
    }

    // === ПРИОРИТЕТНЫЕ ЗАДАЧИ ===
    @Override
    public List<Task> getPrioritizedTasks() {
        return new ArrayList<>(prioritizedTasks);
    }

    // === ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ ===

    /** Обновляет статус эпика на основе статусов его подзадач */
    private void updateEpicStatus(Epic epic) {
        var ids = epic.getSubtaskIds();
        if (ids.isEmpty()) {
            epic.setStatus(Status.NEW);
            return;
        }
        var statuses = ids.stream()
                .map(subtasks::get)
                .filter(Objects::nonNull)
                .map(Subtask::getStatus)
                .collect(Collectors.toSet());

        if (statuses.size() == 1 && statuses.contains(Status.DONE)) {
            epic.setStatus(Status.DONE);
        } else if (statuses.size() == 1 && statuses.contains(Status.NEW)) {
            epic.setStatus(Status.NEW);
        } else {
            epic.setStatus(Status.IN_PROGRESS);
        }
    }

    /** Обновляет время и длительность эпика */
    private void updateEpicTime(Epic epic) {
        List<Subtask> list = getSubtasksOfEpic(epic.getId());
        if (list.isEmpty()) {
            epic.setDuration(java.time.Duration.ZERO);
            epic.setStartTime(null);
            epic.setEndTime(null);
            return;
        }

        java.time.Duration total = java.time.Duration.ZERO;
        LocalDateTime minStart = null;
        LocalDateTime maxEnd = null;

        for (Subtask s : list) {
            if (s.getDuration() != null) {
                total = total.plus(s.getDuration());
            }
            if (s.getStartTime() != null) {
                if (minStart == null || s.getStartTime().isBefore(minStart)) {
                    minStart = s.getStartTime();
                }
            }
            if (s.getEndTime() != null) {
                if (maxEnd == null || s.getEndTime().isAfter(maxEnd)) {
                    maxEnd = s.getEndTime();
                }
            }
        }
        epic.setDuration(total);
        epic.setStartTime(minStart);
        epic.setEndTime(maxEnd);
    }

    /** Проверка пересечения по времени между двумя задачами */
    private boolean isOverlapping(Task a, Task b) {
        if (a == b) return false;
        if (a.getStartTime() == null || a.getDuration() == null) return false;
        if (b.getStartTime() == null || b.getDuration() == null) return false;

        LocalDateTime aStart = a.getStartTime();
        LocalDateTime aEnd = a.getEndTime();
        LocalDateTime bStart = b.getStartTime();
        LocalDateTime bEnd = b.getEndTime();

        // Пересечение по отрезкам [start, end)
        return aStart.isBefore(bEnd) && bStart.isBefore(aEnd);
    }

    /** Проверка, пересекается ли задача с другими */
    private boolean hasIntersection(Task candidate) {
        if (candidate == null || candidate instanceof Epic) return false;
        if (candidate.getStartTime() == null || candidate.getDuration() == null) return false;
        return prioritizedTasks.stream().anyMatch(t -> isOverlapping(candidate, t));
    }

    /** Добавление задачи в множество приоритетных */
    private void addToPrioritized(Task t) {
        if (t != null && !(t instanceof Epic) && t.getStartTime() != null) {
            prioritizedTasks.add(t);
        }
    }

    /** Удаление задачи из множества приоритетных */
    private void removeFromPrioritized(Task t) {
        if (t != null && !(t instanceof Epic)) {
            prioritizedTasks.remove(t);
        }
    }
}
