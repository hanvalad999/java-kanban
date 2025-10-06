import http.HttpTaskServer;
import manager.InMemoryHistoryManager;
import manager.InMemoryTaskManager;
import manager.TaskManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.net.http.HttpClient;

public abstract class HttpTestBase {
    protected HttpTaskServer server;
    protected TaskManager manager;
    protected HttpClient client;

    @BeforeEach
    void setUpServer() throws Exception {
        manager = new InMemoryTaskManager(new InMemoryHistoryManager());
        server = new HttpTaskServer(manager);
        server.start();
        client = HttpClient.newHttpClient(); // Java 11+
    }

    @AfterEach
    void tearDownServer() {
        server.stop();
    }

    protected String url(String pathAndQuery) {
        return "http://localhost:" + http.HttpTaskServer.PORT + pathAndQuery;
    }
}
