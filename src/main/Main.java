package main;

import manager.Managers;
import manager.TaskManager;
import model.Epic;
import model.Status;
import model.Subtask;
import model.Task;

public class Main {
    public static void main(String[] args) {
        TaskManager manager = Managers.getDefault();

        System.out.println("Создаем и добавляем задачи с полными параметрами...");

        // Создаем задачи с явным указанием id и статуса
        Task task1 = new Task("Постирать одежду", "Стиралка + порошок", 1, Status.NEW);
        Task task2 = new Task("Сходить в магазин", "Купить фрукты", 2, Status.IN_PROGRESS);
        manager.createTask(task1);
        manager.createTask(task2);

        // Создаем эпики с явным указанием id
        System.out.println("\nСоздаем и добавляем эпики...");
        Epic epic1 = new Epic("Организация дня рождения", "Для папы", 3);
        manager.createEpic(epic1);

        // Создаем подзадачи с явным указанием id, статуса и epicId
        System.out.println("\nДобавляем подзадачи...");
        Subtask sub1 = new Subtask("Купить часы", "Черные, кожаные", 4, Status.NEW, epic1.getId());
        Subtask sub2 = new Subtask("Пригласить гостей", "Составить список из 20 человек", 5, Status.NEW, epic1.getId());
        manager.createSubtask(sub1);
        manager.createSubtask(sub2);

        // Выводим информацию
        System.out.println("\nТекущее состояние менеджера:");
        printAllTasks(manager);

        // Обновляем статусы
        System.out.println("\nОбновляем статусы подзадач...");
        sub1.setStatus(Status.DONE);
        sub2.setStatus(Status.IN_PROGRESS);
        manager.updateSubtask(sub1);
        manager.updateSubtask(sub2);

        System.out.println("\nПосле обновления статусов:");
        printAllTasks(manager);

        // Удаляем подзадачу
        System.out.println("\nУдаляем подзадачу...");
        manager.deleteSubtask(sub1.getId());

        System.out.println("\nПосле удаления подзадачи:");
        printAllTasks(manager);

        // Формируем историю просмотров
        System.out.println("\nФормируем историю просмотров...");
        manager.getTaskById(task1.getId());
        manager.getEpicById(epic1.getId());
        manager.getSubtaskById(sub2.getId());

        System.out.println("\nИстория просмотров:");
        manager.getHistory().forEach(System.out::println);
    }

    private static void printAllTasks(TaskManager manager) {
        System.out.println("\n=== Все задачи ===");
        manager.getAllTasks().forEach(task ->
                System.out.printf("Task[id=%d, title='%s', status=%s, description='%s']%n",
                        task.getId(), task.getTitle(), task.getStatus(), task.getDescription()));

        System.out.println("\n=== Все эпики ===");
        manager.getAllEpics().forEach(epic -> {
            System.out.printf("Epic[id=%d, title='%s', status=%s, description='%s']%n",
                    epic.getId(), epic.getTitle(), epic.getStatus(), epic.getDescription());
            System.out.println("Подзадачи:");
            manager.getSubtasksOfEpic(epic.getId()).forEach(subtask ->
                    System.out.printf("  Subtask[id=%d, title='%s', status=%s, epicId=%d, description='%s']%n",
                            subtask.getId(), subtask.getTitle(), subtask.getStatus(),
                            subtask.getEpicId(), subtask.getDescription()));
        });

        System.out.println("\n=== Все подзадачи ===");
        manager.getAllSubtasks().forEach(subtask ->
                System.out.printf("Subtask[id=%d, title='%s', status=%s, epicId=%d, description='%s']%n",
                        subtask.getId(), subtask.getTitle(), subtask.getStatus(),
                        subtask.getEpicId(), subtask.getDescription()));
    }
}