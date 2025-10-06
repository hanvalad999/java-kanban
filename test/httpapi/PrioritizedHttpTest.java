package httpapi;

import org.junit.jupiter.api.Test;

import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.*;

public class PrioritizedHttpTest extends HttpTestBase {

    @Test
    void prioritized_sortedByStartTime() throws Exception {
        String t1 = """
        {
          "title":"A",
          "description":"d",
          "status":"NEW",
          "duration":"PT5M",
          "startTime":"2025-10-06T09:00:00"
        }
        """;

        String t2 = """
        {
          "title":"B",
          "description":"d",
          "status":"NEW",
          "duration":"PT5M",
          "startTime":"2025-10-06T08:00:00"
        }
        """;

        assertEquals(201, post("/tasks", t1).statusCode());
        assertEquals(201, post("/tasks", t2).statusCode());

        var resp = client.send(
                HttpRequest.newBuilder(URI.create(url("/prioritized"))).GET().build(),
                HttpResponse.BodyHandlers.ofString()
        );

        assertEquals(200, resp.statusCode());

        String body = resp.body();
        assertTrue(body.indexOf("\"title\":\"B\"") < body.indexOf("\"title\":\"A\""));
    }
}
