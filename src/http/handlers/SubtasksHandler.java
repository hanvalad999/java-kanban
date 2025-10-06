package http.handlers;

import com.sun.net.httpserver.HttpExchange;
import http.BaseHttpHandler;
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
        String path = uri.getPath();
        String query = uri.getQuery();

        try {
            switch (method) {
                case "GET": {
                    if (path.endsWith("/epic") && query != null && query.startsWith("id=")) {
                        int epicId = Integer.parseInt(query.substring(3));
                        sendText(exchange, 200, gson.toJson(manager.getSubtasksOfEpic(epicId)));
                    } else if (query != null && query.startsWith("id=")) {
                        int id = Integer.parseInt(query.substring(3));
                        Subtask s = manager.getSubtaskById(id);
                        if (s == null) { sendNotFound(exchange, "Subtask not found"); return; }
                        sendText(exchange, 200, gson.toJson(s));
                    } else {
                        List<Subtask> list = manager.getAllSubtasks();
                        sendText(exchange, 200, gson.toJson(list));
                    }
                    break;
                }
                case "POST": {
                    String body = readBody(exchange);
                    Subtask incoming = gson.fromJson(body, Subtask.class);
                    if (incoming == null) { sendServerError(exchange, "Invalid JSON"); return; }

                    if (incoming.getId() == 0) {
                        try {
                            Subtask created = manager.createSubtask(incoming);
                            sendText(exchange, 201, gson.toJson(created));
                        } catch (TimeIntersectionException tie) {
                            sendHasIntersections(exchange, tie.getMessage());
                        }
                    } else {
                        try {
                            manager.updateSubtask(incoming);
                            sendText(exchange, 201, gson.toJson(incoming));
                        } catch (TimeIntersectionException tie) {
                            sendHasIntersections(exchange, tie.getMessage());
                        }
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
                    sendServerError(exchange, "Unsupported method");
            }
        } catch (Exception e) {
            sendServerError(exchange, e.getMessage());
        }
    }
}
