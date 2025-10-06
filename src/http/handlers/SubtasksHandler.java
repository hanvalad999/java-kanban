package http.handlers;

import com.sun.net.httpserver.HttpExchange;
import http.BaseHttpHandler;
import manager.NotFoundException;
import manager.TaskManager;
import manager.TimeIntersectionException;
import model.Subtask;

import java.io.IOException;
import java.net.URI;
import java.util.List;

public class SubtasksHandler extends BaseHttpHandler {
    private final TaskManager manager;

    public SubtasksHandler(TaskManager manager) {
        this.manager = manager;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        URI uri = exchange.getRequestURI();
        String query = uri.getQuery();

        try {
            switch (method) {
                case "GET": {
                    if (query != null && query.startsWith("id=")) {
                        int id = Integer.parseInt(query.substring(3));
                        Subtask sub = manager.getSubtaskById(id); // выбросит NotFoundException
                        sendText(exchange, 200, gson.toJson(sub));
                    } else {
                        List<Subtask> list = manager.getAllSubtasks();
                        sendText(exchange, 200, gson.toJson(list));
                    }
                    break;
                }

                case "POST": {
                    String body = readBody(exchange);
                    Subtask incoming = gson.fromJson(body, Subtask.class);
                    if (incoming == null) {
                        sendServerError(exchange, "Invalid JSON");
                        return;
                    }

                    if (incoming.getId() == 0) {
                        Subtask created = manager.createSubtask(incoming);
                        sendText(exchange, 201, gson.toJson(created));
                    } else {
                        manager.updateSubtask(incoming);
                        sendText(exchange, 201, gson.toJson(incoming));
                    }
                    break;
                }

                case "DELETE": {
                    if (query != null && query.startsWith("id=")) {
                        int id = Integer.parseInt(query.substring(3));
                        manager.deleteSubtask(id);
                    } else {
                        manager.clearAllSubtasks();
                    }
                    sendText(exchange, 201, "");
                    break;
                }

                default:
                    sendServerError(exchange, "Unsupported method: " + method);
            }

        } catch (NotFoundException e) {
            sendNotFound(exchange, e.getMessage());
        } catch (TimeIntersectionException e) {
            sendHasIntersections(exchange, e.getMessage());
        } catch (Exception e) {
            sendServerError(exchange, e.getMessage());
        }
    }
}
