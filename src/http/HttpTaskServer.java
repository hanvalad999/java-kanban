package http;

import com.sun.net.httpserver.HttpServer;
import http.handlers.*;
import manager.Managers;
import manager.TaskManager;

import java.io.IOException;
import java.net.InetSocketAddress;

public class HttpTaskServer {
    public static final int PORT = 8080;

    private final HttpServer httpServer;
    private final TaskManager taskManager;

    public HttpTaskServer(TaskManager taskManager) throws IOException {
        this.taskManager = taskManager;

        // создаём HTTP-сервер на порту 8080
        this.httpServer = HttpServer.create(new InetSocketAddress(PORT), 0);

        // регистрируем обработчики (у тебя TaskHandler, без 's')
        httpServer.createContext("/tasks",       new TaskHandler(taskManager));
        httpServer.createContext("/subtasks",    new SubtasksHandler(taskManager));
        httpServer.createContext("/epics",       new EpicsHandler(taskManager));
        httpServer.createContext("/history",     new HistoryHandler(taskManager));
        httpServer.createContext("/prioritized", new PrioritizedHandler(taskManager));
    }

    /** Запуск сервера */
    public void start() {
        httpServer.start();
        System.out.println("HTTP сервер запущен на порту " + PORT);
    }

    /** Остановка сервера */
    public void stop() {
        httpServer.stop(0);
        System.out.println("HTTP сервер остановлен.");
    }

    /** Точка входа в приложение */
    public static void main(String[] args) throws Exception {
        TaskManager taskManager = Managers.getDefault();
        HttpTaskServer httpTaskServer = new HttpTaskServer(taskManager);
        httpTaskServer.start();
    }
}
