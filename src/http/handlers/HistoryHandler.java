package http.handlers;

import com.sun.net.httpserver.HttpExchange;
import http.BaseHttpHandler;
import manager.TaskManager;

import java.io.IOException;

public class HistoryHandler extends BaseHttpHandler {
    private final TaskManager manager;

    public HistoryHandler(TaskManager manager) {
        this.manager = manager;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if ("GET".equals(exchange.getRequestMethod())) {
            try {
                sendText(exchange, 200, gson.toJson(manager.getHistory()));
            } catch (Exception e) {
                sendServerError(exchange, e.getMessage());
            }
        } else {
            sendServerError(exchange, "Unsupported method");
        }
    }
}
