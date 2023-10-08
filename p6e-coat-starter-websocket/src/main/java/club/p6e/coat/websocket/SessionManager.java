package club.p6e.coat.websocket;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Function;

/**
 * @author lidashuang
 * @version 1.0
 */
public class SessionManager {

    private static int CHANNEL_NUM = 20;
    private static ScheduledExecutorService EXECUTOR = null;
    private static final Map<String, Session<?>> SESSIONS = new ConcurrentHashMap<>();
    private static final Map<String, Map<String, Session<?>>> CHANNELS = new ConcurrentHashMap<>();

    public static void init(int num) {
        CHANNEL_NUM = num;
        EXECUTOR = Executors.newScheduledThreadPool(num);
    }

    public static void register(String id, Session<?> session) {
        SESSIONS.put(id, session);
        CHANNELS.computeIfAbsent(
                String.valueOf(id.hashCode() % CHANNEL_NUM),
                k -> new ConcurrentHashMap<>()
        ).put(id, session);
    }

    public static void unregister(String id) {
        SESSIONS.remove(id);
        final Map<String, Session<?>> data = CHANNELS.get(
                String.valueOf(id.hashCode() % CHANNEL_NUM));
        if (data != null) {
            data.remove(id);
        }
    }

    public static Session<?> get(String key) {
        return SESSIONS.get(key);
    }

    public static Iterator<Session<?>> all() {
        return SESSIONS.values().iterator();
    }

    private static List<List<Session<?>>> getChannel() {
        final List<List<Session<?>>> list = new ArrayList<>();
        CHANNELS.forEach((k, v) -> list.add(new ArrayList<>(v.values())));
        return list;
    }

    public static void push(Function<User, Boolean> filter, String id, String type, String content) {
        final List<List<Session<?>>> list = getChannel();
        final Map<String, String> data = new HashMap<>();
        data.put("id", id);
        data.put("type", type);
        data.put("content", content);
        final String wc = JsonUtil.toJson(data);
        for (final List<Session<?>> channels : list) {
            if (channels != null && !channels.isEmpty()) {
                EXECUTOR.submit(() -> {
                    for (final Session<?> session : channels) {
                        final Boolean result = filter.apply(session.getUser());
                        if (result != null && result) {
                            session.push(wc);
                        }
                    }
                });
            }
        }
    }

}
