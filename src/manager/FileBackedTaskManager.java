package manager;

import model.*;

import java.io.*;

import java.util.*;

import static model.TaskType.*;

public class FileBackedTaskManager extends InMemoryTaskManager {

    private final File file;



    public FileBackedTaskManager(File file) {
        super(new InMemoryHistoryManager());
        this.file = file;
    }

    public void save(){
        try (BufferedWriter fileWriter = new BufferedWriter(new FileWriter(this.file))) {
            fileWriter.write("id,type,name,status,description,epic");
            fileWriter.newLine();
            for (Task task : getAllTasks()) {
                fileWriter.write(task.getId() + "," + task.getType() + "," + task.getTitle() + "," + task.getStatus() + "," + task.getDescription() + ",");
                fileWriter.newLine();
            }

            for (Epic epic : getAllEpics()) {
                fileWriter.write(epic.getId() + "," + epic.getType() + "," + epic.getTitle() + "," + epic.getStatus() + "," + epic.getDescription()+ ",");
                fileWriter.newLine();
            }

            for (Subtask subtask : getAllSubtasks()) {
                fileWriter.write(subtask.getId() + "," + subtask.getType() + "," + subtask.getTitle() + "," + subtask.getStatus() + "," + subtask.getDescription() + "," + subtask.getEpicId());
                fileWriter.newLine();
            }

        } catch (IOException e) {
            System.out.println("Ошибка!");
        }
    }
    private static Task fromString(String value) {
        String[] params = value.split(",");
        int id = Integer.parseInt(params[0]);
        TaskType type = TaskType.valueOf(params[1]);
        String title = params[2];
        Status status = Status.valueOf(params[3]);
        String description = params[4];

        if (type == TASK) {
            Task task = new Task(title, description, id, status);
            return task;
        } else if (type == EPIC) {
            Epic epic = new Epic(title, description, id);
            return epic;
        } else {
            int epicID = Integer.parseInt(params[5]);
            Subtask subtask = new Subtask(title, description, id, status, epicID);
            return subtask;
        }

    }

    public static FileBackedTaskManager loadFromFile(File file) {
        FileBackedTaskManager manager = new FileBackedTaskManager(file);

        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(file))) {
            bufferedReader.readLine();
            String line;

            while ((line = bufferedReader.readLine()) != null) {
                if(line.isEmpty()){
                    break;
                }
                Task task = fromString(line);

                if (task instanceof Epic) { // долго думал грамотно использовать fromString(line) и узнал об "instanceof"
                    manager.addEpic((Epic) task);
                } else if (task instanceof Subtask) {
                    manager.addSubtask((Subtask) task);
                } else {
                    manager.addTask(task);
                }
            }

        } catch (IOException e) {
            System.out.println("Не удалось считать данные из файла.");
        }
        return manager;
    }

    public Task addTask(Task task){
        return super.createTask(task);
    }

    public Epic addEpic(Epic epic) {
        return super.createEpic(epic);
    }

    public Subtask addSubtask(Subtask subtask) {
        return super.createSubtask(subtask);
    }

    @Override
    public Task createTask(Task task) {
        super.createTask(task);
        save();
        return task;
    }

    @Override
    public Epic createEpic(Epic epic) {
        super.createEpic(epic);
        save();
        return epic;
    }

    @Override
    public Subtask createSubtask(Subtask subtask) {
        super.createSubtask(subtask);
        save();
        return subtask;
    }

    @Override
    public void updateTask(Task updatedTask) {
        super.updateTask(updatedTask);
        save();
    }

    @Override
    public void updateEpic(Epic updatedEpic) {
        super.updateEpic(updatedEpic);
        save();
    }

    @Override
    public void updateSubtask(Subtask updatedSubtask) {
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
