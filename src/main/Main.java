package main;

import manager.TaskManager;
import model.Epic;
import model.Status;
import model.Subtask;
import model.Task;

public class Main {
    public static void main(String[] args) {
        TaskManager manager = new TaskManager();

        // Обычные задачи
        Task task1 = manager.createTask(new Task("Постирать одежду", "Стиралка + порошок", 0, Status.NEW));
        Task task2 = manager.createTask(new Task("Сходить в магазин", "Купить фрукты", 0, Status.NEW));

        // Эпики и подзадачи
        Epic epic1 = manager.createEpic(new Epic("Организация дня рождения", "Для папы", 0));
        Subtask sub1 = manager.createSubtask(new Subtask("Купить часы", "Черный", 0, Status.NEW, epic1.getId()));
        Subtask sub2 = manager.createSubtask(new Subtask("Пригласить гостей", "Позвонить друзьям", 0, Status.NEW, epic1.getId()));

        Epic epic2 = manager.createEpic(new Epic("Путешествие", "Дубай", 0));
        Subtask sub3 = manager.createSubtask(new Subtask("Полетим летом", "Купить летом билеты", 0, Status.NEW, epic2.getId()));

        // Вывод
        System.out.println("Все эпики:");
        manager.getAllEpics().forEach(System.out::println);

        System.out.println("\nПодзадачи первого эпика:");
        manager.getSubtasksOfEpic(epic1.getId()).forEach(System.out::println);


        sub1.setStatus(Status.DONE);
        sub2.setStatus(Status.IN_PROGRESS);
        manager.updateSubtask(sub1);
        manager.updateSubtask(sub2);

        System.out.println("\nОбновлённый эпик:");
        System.out.println(manager.getEpicById(epic1.getId()));


        manager.deleteSubtask(sub1.getId());

        System.out.println("\nПосле удаления подзадачи:");
        System.out.println(manager.getEpicById(epic1.getId()));
    }
}
