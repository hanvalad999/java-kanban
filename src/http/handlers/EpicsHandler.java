package http.handlers;

import com.sun.net.httpserver.HttpExchange;
import http.BaseHttpHandler;
import manager.NotFoundException;
import manager.TaskManager;
import model.Epic;

import java.io.IOException;
import java.net.URI;
import java.util.List;

public class EpicsHandler extends BaseHttpHandler {
    private final TaskManager manager;

    public EpicsHandler(TaskManager manager) {
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
                        Epic epic = manager.getEpicById(id); // может выбросить NotFoundException
                        sendText(exchange, 200, gson.toJson(epic));
                    } else {
                        List<Epic> list = manager.getAllEpics();
                        sendText(exchange, 200, gson.toJson(list));
                    }
                    break;
                }

                case "POST": {
                    String body = readBody(exchange);
                    Epic incoming = gson.fromJson(body, Epic.class);
                    if (incoming == null) {
                        sendServerError(exchange, "Invalid JSON");
                        return;
                    }

                    if (incoming.getId() == 0) {
                        Epic created = manager.createEpic(incoming);
                        sendText(exchange, 201, gson.toJson(created));
                    } else {
                        manager.updateEpic(incoming);
                        sendText(exchange, 201, gson.toJson(incoming));
                    }
                    break;
                }

                case "DELETE": {
                    if (query != null && query.startsWith("id=")) {
                        int id = Integer.parseInt(query.substring(3));
                        manager.deleteEpic(id);
                    } else {
                        manager.clearAllEpics();
                    }
                    sendText(exchange, 201, "");
                    break;
                }

                default:
                    sendServerError(exchange, "Unsupported method: " + method);
            }
        } catch (NotFoundException e) {
            sendNotFound(exchange, e.getMessage());
        } catch (Exception e) {
            sendServerError(exchange, e.getMessage());
        }
    }
}
