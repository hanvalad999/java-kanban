// Managers.java
package manager;

import com.google.gson.*;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Managers {
    public static TaskManager getDefault() {
        return new InMemoryTaskManager(getDefaultHistory());
    }
    public static HistoryManager getDefaultHistory() {
        return new InMemoryHistoryManager();
    }

    // общий Gson с адаптерами
    private static final Gson GSON = buildGson();
    public static Gson getGson() { return GSON; }

    // пытался решит проблему что gson из коробки не понимает и падает JsonSyntaxException(отсюда 500)
    private static Gson buildGson() {
        GsonBuilder b = new GsonBuilder().serializeNulls();

        b.registerTypeAdapter(LocalDateTime.class,
                (JsonSerializer<LocalDateTime>)(src,t,ctx) ->
                        src==null?JsonNull.INSTANCE:new JsonPrimitive(src.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)));
        b.registerTypeAdapter(LocalDateTime.class,
                (JsonDeserializer<LocalDateTime>)(json,t,ctx) ->
                        (json==null||json.isJsonNull())?null:LocalDateTime.parse(json.getAsString(), DateTimeFormatter.ISO_LOCAL_DATE_TIME));

        b.registerTypeAdapter(Duration.class,
                (JsonSerializer<Duration>)(src,t,ctx) ->
                        src==null?JsonNull.INSTANCE:new JsonPrimitive(src.toString()));
        b.registerTypeAdapter(Duration.class,
                (JsonDeserializer<Duration>)(json,t,ctx) ->
                        (json==null||json.isJsonNull())?null:Duration.parse(json.getAsString()));

        return b.create();
    }
}
