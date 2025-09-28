package manager;

import model.*;

import java.io.*;
import java.lang.reflect.Field;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class FileBackedTaskManager extends InMemoryTaskManager {

    private static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    private final File file;

    private FileBackedTaskManager(File file) {
        super(new InMemoryHistoryManager());
        this.file = file;
    }

    private static final String HEADER =
            "id,type,name,status,description,durationMinutes,startTime,epic";

    private static String escape(String s) {
        if (s == null) {
            return "";
        }
        return s.replace("\n", " ").replace(",", "&#44;");
    }

    private static String unescape(String s) {
        if (s == null) {
            return "";
        }
        return s.replace("&#44;", ",");
    }

    private String taskToCsv(Task task) {
        String type = (task instanceof Epic) ? TaskType.EPIC.name()
                : (task instanceof Subtask) ? TaskType.SUBTASK.name()
                : TaskType.TASK.name();
        String duration = (task.getDuration() == null) ? "" : String.valueOf(task.getDuration().toMinutes());
        String start = (task.getStartTime() == null) ? "" : task.getStartTime().toString();
        String epicId = "";
        if (task instanceof Subtask st) {
            epicId = String.valueOf(st.getEpicId());
        }
        return String.join(",",
                String.valueOf(task.getId()),
                type,
                escape(task.getTitle()),
                task.getStatus().name(),
                escape(task.getDescription()),
                duration,
                start,
                epicId
        );
    }

    private Task fromString(String line) {
        String[] p = line.split(",", -1);
        int id = Integer.parseInt(p[0]);
        TaskType type = TaskType.valueOf(p[1]);
        String title = unescape(p[2]);
        Status status = Status.valueOf(p[3]);
        String description = unescape(p[4]);
        Duration duration = p[5].isEmpty() ? null : Duration.ofMinutes(Long.parseLong(p[5]));
        LocalDateTime start = p[6].isEmpty() ? null : LocalDateTime.parse(p[6]);
        switch (type) {
            case TASK -> {
                Task t = new Task(title, description, id, status);
                t.setDuration(duration);
                t.setStartTime(start);
                return t;
            }
            case EPIC -> {
                Epic e = new Epic(title, description, id);
                e.setStatus(status);
                return e;
            }
            case SUBTASK -> {
                int epicId = Integer.parseInt(p[7]);
                Subtask s = new Subtask(title, description, id, status, epicId);
                s.setDuration(duration);
                s.setStartTime(start);
                return s;
            }
            default -> throw new IllegalStateException("Неизвестный тип задачи: " + type);
        }
    }

    public void save() {
        try (BufferedWriter w = new BufferedWriter(new FileWriter(this.file))) {
            w.write(HEADER);
            w.newLine();
            for (Task t : getAllTasks()) {
                w.write(taskToCsv(t));
                w.newLine();
            }
            for (Epic e : getAllEpics()) {
                w.write(taskToCsv(e));
                w.newLine();
            }
            for (Subtask s : getAllSubtasks()) {
                w.write(taskToCsv(s));
                w.newLine();
            }
        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка при сохранении файла", e);
        }
    }

    public static FileBackedTaskManager loadFromFile(File file) {
        FileBackedTaskManager m = new FileBackedTaskManager(file);

        List<Task> tasks = new ArrayList<>();
        List<Epic> epics = new ArrayList<>();
        List<Subtask> subtasks = new ArrayList<>();
        int maxId = 0;

        if (!file.exists()) {
            return m;
        }

        try (BufferedReader r = new BufferedReader(new FileReader(file))) {
            String header = r.readLine();
            String line;
            while ((line = r.readLine()) != null) {
                if (line.isBlank()) continue;
                Task t = m.fromString(line);
                maxId = Math.max(maxId, t.getId());
                if (t instanceof Epic e) {
                    epics.add(e);
                } else if (t instanceof Subtask s) {
                    subtasks.add(s);
                } else {
                    tasks.add(t);
                }
            }
        } catch (IOException e) {
            throw new ManagerLoadException("Не удалось считать данные из файла", e);
        }

        epics.sort((a, b) -> Integer.compare(a.getId(), b.getId()));
        tasks.sort((a, b) -> Integer.compare(a.getId(), b.getId()));
        subtasks.sort((a, b) -> Integer.compare(a.getId(), b.getId()));

        for (Epic e : epics) {
            setNextId(m, e.getId());
            m.createEpic(e);
        }
        for (Task t : tasks) {
            setNextId(m, t.getId());
            m.createTask(t);
        }
        for (Subtask s : subtasks) {
            setNextId(m, s.getId());
            m.createSubtask(s);
        }

        setNextId(m, maxId + 1);
        return m;
    }

    private static void setNextId(InMemoryTaskManager mgr, int value) {
        try {
            Field f = InMemoryTaskManager.class.getDeclaredField("nextId");
            f.setAccessible(true);
            f.setInt(mgr, value);
        } catch (Exception e) {
            throw new ManagerLoadException("Не удалось установить nextId", e);
        }
    }

    public Task addTask(Task task) {
        return super.createTask(task);
    }

    public Epic addEpic(Epic epic) {
        return super.createEpic(epic);
    }

    public Subtask addSubtask(Subtask subtask) {
        return super.createSubtask(subtask);
    }

    @Override
    public Task createTask(Task task) throws TimeIntersectionException {
        Task t = super.createTask(task);
        save();
        return t;
    }

    @Override
    public Epic createEpic(Epic epic) {
        Epic e = super.createEpic(epic);
        save();
        return e;
    }

    @Override
    public Subtask createSubtask(Subtask subtask) throws TimeIntersectionException {
        Subtask s = super.createSubtask(subtask);
        save();
        return s;
    }

    @Override
    public void updateTask(Task updatedTask) throws TimeIntersectionException {
        super.updateTask(updatedTask);
        save();
    }

    @Override
    public void updateEpic(Epic updatedEpic) {
        super.updateEpic(updatedEpic);
        save();
    }

    @Override
    public void updateSubtask(Subtask updatedSubtask) throws TimeIntersectionException {
        super.updateSubtask(updatedSubtask);
        save();
    }

    @Override
    public void deleteTask(int id) {
        super.deleteTask(id);
        save();
    }

    @Override
    public void deleteEpic(int id) {
        super.deleteEpic(id);
        save();
    }

    @Override
    public void deleteSubtask(int id) {
        super.deleteSubtask(id);
        save();
    }

    @Override
    public void clearAllTasks() {
        super.clearAllTasks();
        save();
    }

    @Override
    public void clearAllEpics() {
        super.clearAllEpics();
        save();
    }

    @Override
    public void clearAllSubtasks() {
        super.clearAllSubtasks();
        save();
    }
}
