package manager;

import com.google.gson.*;
import java.lang.reflect.Type;
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

    private static final Gson GSON = buildGson();

    public static Gson getGson() {
        return GSON;
    }

    // пытался решит проблему что gson из коробки не понимает и падает JsonSyntaxException(отсюда 500)
    private static Gson buildGson() {
        GsonBuilder builder = new GsonBuilder().serializeNulls();

        builder.registerTypeAdapter(LocalDateTime.class, new JsonSerializer<LocalDateTime>() {
            @Override
            public JsonElement serialize(LocalDateTime src, Type typeOfSrc, JsonSerializationContext context) {
                return src == null ? JsonNull.INSTANCE
                        : new JsonPrimitive(src.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            }
        });
        builder.registerTypeAdapter(LocalDateTime.class, new JsonDeserializer<LocalDateTime>() {
            @Override
            public LocalDateTime deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
                    throws JsonParseException {
                if (json == null || json.isJsonNull()) return null;
                return LocalDateTime.parse(json.getAsString(), DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            }
        });

        builder.registerTypeAdapter(Duration.class, new JsonSerializer<Duration>() {
            @Override
            public JsonElement serialize(Duration src, Type typeOfSrc, JsonSerializationContext context) {
                return src == null ? JsonNull.INSTANCE : new JsonPrimitive(src.toString());
            }
        });
        builder.registerTypeAdapter(Duration.class, new JsonDeserializer<Duration>() {
            @Override
            public Duration deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
                    throws JsonParseException {
                if (json == null || json.isJsonNull()) return null;
                return Duration.parse(json.getAsString());
            }
        });

        return builder.create();
    }
}
