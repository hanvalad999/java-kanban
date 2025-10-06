package httpapi;

import org.junit.jupiter.api.Test;

import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.*;

public class HistoryHttpTest extends HttpTestBase {

    @Test
    void history_changes_after_getById() throws Exception {
        // создаём задачу
        String json = """
        {
          "title":"T1",
          "description":"d",
          "status":"NEW",
          "duration":"PT5M",
          "startTime":"2025-10-06T10:00:00"
        }
        """;

        var create = client.send(
                HttpRequest.newBuilder(URI.create(url("/tasks")))
                        .header("Content-Type","application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(json)).build(),
                HttpResponse.BodyHandlers.ofString()
        );
        assertEquals(201, create.statusCode());
        int id = extractId(create.body());

        // до просмотра — история вернётся пустой список/без этой задачи
        var h1 = client.send(
                HttpRequest.newBuilder(URI.create(url("/history"))).GET().build(),
                HttpResponse.BodyHandlers.ofString()
        );
        assertEquals(200, h1.statusCode());

        // открыть задачу по id
        var getById = client.send(
                HttpRequest.newBuilder(URI.create(url("/tasks?id=" + id))).GET().build(),
                HttpResponse.BodyHandlers.ofString()
        );
        assertEquals(200, getById.statusCode());

        // после просмотра — в истории должна появиться T1
        var h2 = client.send(
                HttpRequest.newBuilder(URI.create(url("/history"))).GET().build(),
                HttpResponse.BodyHandlers.ofString()
        );
        assertEquals(200, h2.statusCode());
        assertTrue(h2.body().contains("\"title\":\"T1\""));
    }
}
