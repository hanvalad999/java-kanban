package main;

import manager.TaskManager;
import manager.FileBackedTaskManager;
import model.*;

import java.io.File;

public class Main {
    public static void main(String[] args) {

        TaskManager manager = new FileBackedTaskManager(new File("tasks.csv"));

        System.out.println("Создаём и добавляем задачи...");
        
        Task task1 = new Task("Постирать одежду", "Стиралка + порошок", 0, Status.NEW);
        Task task2 = new Task("Сходить в магазин", "Купить фрукты", 0, Status.IN_PROGRESS);
        manager.createTask(task1);
        manager.createTask(task2);

        System.out.println("\nСоздаём и добавляем эпики...");
        Epic epic1 = new Epic("Организация дня рождения", "Для папы", 0);
        manager.createEpic(epic1); // после этого у epic1 появится реальный id

        System.out.println("\nДобавляем подзадачи...");
        Subtask sub1 = new Subtask("Купить часы", "Чёрные, кожаные", 0, Status.NEW, epic1.getId());
        Subtask sub2 = new Subtask("Пригласить гостей", "Список ~20 человек", 0, Status.NEW, epic1.getId());
        manager.createSubtask(sub1);
        manager.createSubtask(sub2);

        System.out.println("\nТекущее состояние менеджера:");
        printAll(manager);

        System.out.println("\nОбновляем статусы подзадач...");
        sub1.setStatus(Status.DONE);
        sub2.setStatus(Status.IN_PROGRESS);
        manager.updateSubtask(sub1);
        manager.updateSubtask(sub2);

        System.out.println("\nПосле обновления статусов:");
        printAll(manager);

        System.out.println("\nУдаляем подзадачу...");
        manager.deleteSubtask(sub1.getId());

        System.out.println("\nПосле удаления подзадачи:");
        printAll(manager);

        System.out.println("\nФормируем историю просмотров...");
        manager.getTaskById(task1.getId());
        manager.getEpicById(epic1.getId());
        manager.getSubtaskById(sub2.getId());

        System.out.println("\nИстория просмотров:");
        manager.getHistory().forEach(System.out::println);

        System.out.println("\nДанные сохранены в файл: tasks.csv");
    }

    private static void printAll(TaskManager manager) {
        System.out.println("\n=== Все задачи ===");
        for (Task t : manager.getAllTasks()) {
            System.out.printf("Task[id=%d, title='%s', status=%s, description='%s']%n",
                    t.getId(), t.getTitle(), t.getStatus(), t.getDescription());
        }

        System.out.println("\n=== Все эпики ===");
        for (Epic e : manager.getAllEpics()) {
            System.out.printf("Epic[id=%d, title='%s', status=%s, description='%s']%n",
                    e.getId(), e.getTitle(), e.getStatus(), e.getDescription());
            System.out.println("Подзадачи:");
            for (Subtask s : manager.getSubtasksOfEpic(e.getId())) {
                System.out.printf("  Subtask[id=%d, title='%s', status=%s, epicId=%d, description='%s']%n",
                        s.getId(), s.getTitle(), s.getStatus(), s.getEpicId(), s.getDescription());
            }
        }

        System.out.println("\n=== Все подзадачи ===");
        for (Subtask s : manager.getAllSubtasks()) {
            System.out.printf("Subtask[id=%d, title='%s', status=%s, epicId=%d, description='%s']%n",
                    s.getId(), s.getTitle(), s.getStatus(), s.getEpicId(), s.getDescription());
        }
    }
}
