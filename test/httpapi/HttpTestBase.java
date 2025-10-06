package httpapi;

import http.HttpTaskServer;
import manager.Managers;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URI;

/**
 * Базовый класс для всех HTTP-тестов (TasksHttpTest, EpicsHttpTest и др.)
 */
public abstract class HttpTestBase {

    protected HttpTaskServer server;
    protected HttpClient client;

    @BeforeEach
    void startServer() throws IOException {
        server = new HttpTaskServer(Managers.getDefault());
        server.start();
        client = HttpClient.newHttpClient();
        System.out.println("HTTP сервер запущен на порту 8080");
    }

    @AfterEach
    void stopServer() {
        server.stop();
        System.out.println("HTTP сервер остановлен.");
    }

    // --- Вспомогательные методы ---

    /** Базовый URL (меняется при необходимости) */
    protected String url(String path) {
        return "http://localhost:8080" + path;
    }

    /** Упрощённая отправка POST-запроса */
    protected HttpResponse<String> post(String path, String json) throws IOException, InterruptedException {
        var req = HttpRequest.newBuilder(URI.create(url(path)))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();
        return client.send(req, HttpResponse.BodyHandlers.ofString());
    }

    /** Достаёт id из JSON-ответа */
    protected int extractId(String json) {
        int pos = json.indexOf("\"id\":");
        if (pos < 0) return -1;
        int start = pos + 5;
        int endComma = json.indexOf(',', start);
        int endBrace = json.indexOf('}', start);
        int end = (endComma > 0) ? Math.min(endComma, endBrace) : endBrace;
        if (end < 0) end = json.length();
        try {
            return Integer.parseInt(json.substring(start, end).trim());
        } catch (NumberFormatException e) {
            return -1;
        }
    }
}
