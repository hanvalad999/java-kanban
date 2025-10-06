package httpapi;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.net.http.*;

public class EpicsHttpTest extends HttpTestBase {

    @Test
    void createEpic_and_listSubtasks_empty() throws Exception {
        String epicJson = """
        {"title":"Epic A","description":"d","status":"NEW"}
        """;
        var create = client.send(
                HttpRequest.newBuilder(URI.create(url("/epics")))
                        .header("Content-Type","application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(epicJson)).build(),
                HttpResponse.BodyHandlers.ofString()
        );
        Assertions.assertEquals(201, create.statusCode());
        int epicId = extractId(create.body());

        var subsOfEpic = client.send(
                HttpRequest.newBuilder(URI.create(url("/epics/subtasks?epicId="+epicId))).GET().build(),
                HttpResponse.BodyHandlers.ofString()
        );
        Assertions.assertEquals(200, subsOfEpic.statusCode());
        Assertions.assertTrue(subsOfEpic.body().startsWith("[")); // пустой список — "[]"
    }

    @Test
    void getUnknown_returns404() throws Exception {
        var resp = client.send(
                HttpRequest.newBuilder(URI.create(url("/epics?id=999999"))).GET().build(),
                HttpResponse.BodyHandlers.ofString()
        );
        Assertions.assertEquals(404, resp.statusCode());
    }
}
