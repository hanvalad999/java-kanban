package httpapi;

import org.junit.jupiter.api.Test;
import java.net.http.HttpResponse;
import static org.junit.jupiter.api.Assertions.*;

public class EpicsHttpTest extends HttpTestBase {

    @Test
    void createEpic_and_listSubtasks_empty() throws Exception {
        int epicId = createEpic("""
            {"title":"Epic A","description":"d","status":"NEW"}
        """);

        var subsOfEpic = client.send(
                java.net.http.HttpRequest.newBuilder(
                        java.net.URI.create(url("/epics/subtasks?epicId=" + epicId))
                ).GET().build(),
                java.net.http.HttpResponse.BodyHandlers.ofString()
        );

        assertEquals(200, subsOfEpic.statusCode());
        assertTrue(subsOfEpic.body().startsWith("["));
    }

    @Test
    void getUnknown_returns404() throws Exception {
        var resp = client.send(
                java.net.http.HttpRequest.newBuilder(
                        java.net.URI.create(url("/epics?id=999999"))
                ).GET().build(),
                java.net.http.HttpResponse.BodyHandlers.ofString()
        );
        assertEquals(404, resp.statusCode());
    }

    // Вспомогательный метод
    protected int createEpic(String json) throws Exception {
        var response = post("/epics", json);
        assertEquals(201, response.statusCode());
        return extractId(response.body());
    }
}
