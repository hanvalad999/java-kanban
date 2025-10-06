import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class TasksHttpTest extends HttpTestBase {

    @Test
    void createAndGetTask_success() throws Exception {
        String json = """
            {"name":"Task A","description":"d","status":"NEW","duration":30,"startTime":"2025-10-06T10:00:00"}
        """;

        var req = HttpRequest.newBuilder(URI.create(url("/tasks")))
                .header("Content-Type","application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        var resp = client.send(req, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, resp.statusCode());
        assertTrue(resp.body().contains("\"name\":\"Task A\""));

        var getAll = client.send(
                HttpRequest.newBuilder(URI.create(url("/tasks"))).GET().build(),
                HttpResponse.BodyHandlers.ofString()
        );
        assertEquals(200, getAll.statusCode());
        assertTrue(getAll.body().contains("Task A"));
    }

    @Test
    void getTask_notFound_returns404() throws Exception {
        var notFound = client.send(
                HttpRequest.newBuilder(URI.create(url("/tasks?id=999999"))).GET().build(),
                HttpResponse.BodyHandlers.ofString()
        );
        assertEquals(404, notFound.statusCode());
    }
}
