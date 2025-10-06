package httpapi;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class SubtasksHttpTest extends HttpTestBase {

    @Test
    void createAndGetSubtask_success() throws Exception {
        // сначала создаём эпик, чтобы была epicId
        String epicJson = """
        {"title":"E1","description":"d","status":"NEW"}
        """;
        var epicResp = client.send(
                HttpRequest.newBuilder(URI.create(url("/epics")))
                        .header("Content-Type","application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(epicJson))
                        .build(),
                HttpResponse.BodyHandlers.ofString()
        );
        assertEquals(201, epicResp.statusCode());
        int epicId = extractId(epicResp.body()); // <-- используем метод из HttpTestBase

        String subJson = """
        {"title":"S1","description":"sd","status":"NEW",
         "duration":"PT15M","startTime":"2025-10-06T11:00:00","epicId":%d}
        """.formatted(epicId);

        var create = client.send(
                HttpRequest.newBuilder(URI.create(url("/subtasks")))
                        .header("Content-Type","application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(subJson))
                        .build(),
                HttpResponse.BodyHandlers.ofString()
        );
        assertEquals(201, create.statusCode());

        var getAll = client.send(
                HttpRequest.newBuilder(URI.create(url("/subtasks"))).GET().build(),
                HttpResponse.BodyHandlers.ofString()
        );
        assertEquals(200, getAll.statusCode());
        assertTrue(getAll.body().contains("\"title\":\"S1\""));
    }

    @Test
    void getUnknown_returns404() throws Exception {
        var resp = client.send(
                HttpRequest.newBuilder(URI.create(url("/subtasks?id=999999"))).GET().build(),
                HttpResponse.BodyHandlers.ofString()
        );
        assertEquals(404, resp.statusCode());
    }
}
