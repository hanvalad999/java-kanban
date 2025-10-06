package httpapi;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class TasksHttpTest extends HttpTestBase {

    @Test
    void createAndGetTask_success() throws Exception {
        // JSON как строковый блок (Java 15+). Если у тебя Java < 15 — ниже дам вариант одной строкой.
        String json = """
        {
          "title": "Task A",
          "description": "d",
          "status": "NEW",
          "duration": "PT30M",
          "startTime": "2025-10-06T10:00:00"
        }
        """;

        // POST /tasks
        var req = HttpRequest.newBuilder(URI.create(url("/tasks")))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        var resp = client.send(req, HttpResponse.BodyHandlers.ofString());
        System.out.println("POST /tasks -> " + resp.statusCode() + " | body=" + resp.body());
        Assertions.assertEquals(201, resp.statusCode());

        // GET /tasks
        var getAll = client.send(
                HttpRequest.newBuilder(URI.create(url("/tasks"))).GET().build(),
                HttpResponse.BodyHandlers.ofString()
        );
        Assertions.assertEquals(200, getAll.statusCode());
        Assertions.assertTrue(getAll.body().contains("\"title\":\"Task A\""));
    }

    @Test
    void getTask_notFound_returns404() throws Exception {
        var notFound = client.send(
                HttpRequest.newBuilder(URI.create(url("/tasks?id=999999"))).GET().build(),
                HttpResponse.BodyHandlers.ofString()
        );
        Assertions.assertEquals(404, notFound.statusCode());
    }
}
