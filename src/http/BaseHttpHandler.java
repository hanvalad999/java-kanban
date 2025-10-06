package http;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import manager.Managers;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public abstract class BaseHttpHandler implements HttpHandler
{
    // используем общий Gson с адаптерами из Managers
    protected final Gson gson = Managers.getGson();

    protected void sendText(HttpExchange exchange, int statusCode, String body) throws IOException
    {
        byte[] resp = (body == null) ? new byte[0] : body.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Content-Type", "application/json; charset=utf-8");
        exchange.sendResponseHeaders(statusCode, resp.length);
        try (OutputStream os = exchange.getResponseBody())
        {
            os.write(resp);
        }
    }

    protected void sendNotFound(HttpExchange exchange, String message) throws IOException
    {
        sendText(exchange, 404, jsonMessage(message));
    }

    protected void sendHasIntersections(HttpExchange exchange, String message) throws IOException
    {
        sendText(exchange, 406, jsonMessage(message));
    }

    protected void sendServerError(HttpExchange exchange, String message) throws IOException
    {
        sendText(exchange, 500, jsonMessage(message));
    }

    protected String readBody(HttpExchange exchange) throws IOException
    {
        try (InputStream is = exchange.getRequestBody())
        {
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    protected static String jsonMessage(String message)
    {
        if (message == null)
        {
            message = "";
        }
        return "{\"message\":\"" + message.replace("\"", "\\\"") + "\"}";
    }
}
