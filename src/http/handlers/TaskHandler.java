package http.handlers;

import com.sun.net.httpserver.HttpExchange;
import http.BaseHttpHandler;
import manager.NotFoundException;
import manager.TaskManager;
import manager.TimeIntersectionException;
import model.Task;

import java.io.IOException;
import java.net.URI;
import java.util.List;

public class TaskHandler extends BaseHttpHandler {
    private final TaskManager manager;

    public TaskHandler(TaskManager manager) {
        this.manager = manager;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            String method = exchange.getRequestMethod();
            URI uri = exchange.getRequestURI();
            String query = uri.getQuery();

            switch (method) {
                case "GET": {
                    if (query != null && query.startsWith("id=")) {
                        int id = Integer.parseInt(query.substring(3));
                        Task task = manager.getTaskById(id);
                        sendText(exchange, 200, gson.toJson(task));
                    } else {
                        List<Task> tasks = manager.getAllTasks();
                        sendText(exchange, 200, gson.toJson(tasks));
                    }
                    break;
                }

                case "POST": {
                    String body = readBody(exchange);
                    Task incoming = gson.fromJson(body, Task.class);
                    if (incoming == null) {
                        sendServerError(exchange, "Invalid JSON: empty body");
                        return;
                    }

                    if (incoming.getId() == 0) {
                        Task created = manager.createTask(incoming);
                        sendText(exchange, 201, gson.toJson(created));
                    } else {
                        manager.updateTask(incoming);
                        sendText(exchange, 201, gson.toJson(incoming));
                    }
                    break;
                }

                case "DELETE": {
                    if (query != null && query.startsWith("id=")) {
                        int id = Integer.parseInt(query.substring(3));
                        manager.deleteTask(id);
                    } else {
                        manager.clearAllTasks();
                    }
                    sendText(exchange, 201, "");
                    break;
                }

                default:
                    sendServerError(exchange, "Unsupported method: " + method);
            }

            // --- Отдельная обработка исключений ---
        } catch (NotFoundException nfe) {
            sendNotFound(exchange, nfe.getMessage());

        } catch (TimeIntersectionException tie) {
            sendHasIntersections(exchange, tie.getMessage());

        } catch (com.google.gson.JsonSyntaxException jse) {
            jse.printStackTrace(); // 🔍 печатаем стек в консоль
            sendServerError(exchange, "JsonSyntaxException: " + String.valueOf(jse.getMessage()));

        } catch (Exception e) {
            e.printStackTrace(); // 🔍 печатаем стек в консоль
            sendServerError(exchange, e.getClass().getSimpleName() + ": " + String.valueOf(e.getMessage()));
        }
    }
}
