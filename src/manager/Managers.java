package manager;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonNull;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializer;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Managers {

    private Managers() {
    }

    public static TaskManager getDefault() {
        return new InMemoryTaskManager(getDefaultHistory());
    }

    public static HistoryManager getDefaultHistory() {
        return new InMemoryHistoryManager();
    }

    // общий Gson с адаптерами
    private static final Gson GSON = buildGson();

    public static Gson getGson() {
        return GSON;
    }

    // Gson из коробки не умеет Duration/LocalDateTime — регистрируем адаптеры
    private static Gson buildGson() {
        GsonBuilder b = new GsonBuilder().serializeNulls();

        b.registerTypeAdapter(
                LocalDateTime.class,
                (JsonSerializer<LocalDateTime>) (src, t, ctx) ->
                        src == null
                                ? JsonNull.INSTANCE
                                : new JsonPrimitive(src.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
        );

        b.registerTypeAdapter(
                LocalDateTime.class,
                (JsonDeserializer<LocalDateTime>) (json, t, ctx) ->
                        (json == null || json.isJsonNull())
                                ? null
                                : LocalDateTime.parse(json.getAsString(), DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        );

        b.registerTypeAdapter(
                Duration.class,
                (JsonSerializer<Duration>) (src, t, ctx) ->
                        src == null ? JsonNull.INSTANCE : new JsonPrimitive(src.toString())
        );

        b.registerTypeAdapter(
                Duration.class,
                (JsonDeserializer<Duration>) (json, t, ctx) ->
                        (json == null || json.isJsonNull()) ? null : Duration.parse(json.getAsString())
        );

        return b.create();
    }
}
