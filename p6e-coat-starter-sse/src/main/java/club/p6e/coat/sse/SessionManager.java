package club.p6e.coat.sse;

import club.p6e.coat.common.utils.JsonUtil;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.function.Function;

/**
 * 会话管理器
 *
 * @author lidashuang
 * @version 1.0
 */
public final class SessionManager {

    /**
     * 频道（线程）数量
     */
    private static int CHANNEL_NUM = 15;

    /**
     * 线程池对象
     */
    private static ScheduledThreadPoolExecutor EXECUTOR = null;

    /**
     * 会话对象
     */
    private static final Map<String, Session> SESSIONS = new ConcurrentHashMap<>();

    /**
     * 组对象
     */
    private static final Map<String, Map<String, Session>> GROUPS = new ConcurrentHashMap<>();

    /**
     * 初始化方法
     *
     * @param num 频道（线程）数量
     */
    public synchronized static void init(int num) {
        num = num < 0 ? 15 : num;
        if (EXECUTOR != null && CHANNEL_NUM != num) {
            EXECUTOR.shutdown();
            EXECUTOR = null;
        }
        if (EXECUTOR == null) {
            EXECUTOR = new ScheduledThreadPoolExecutor(num, r ->
                    new Thread(r, "P6E-SSE-SESSION-MANAGER-THREAD-" + r.hashCode()));
            CHANNEL_NUM = num;
        }
    }

    /**
     * 注册会话
     *
     * @param id      会话编号
     * @param session 会话对象
     */
    public static void register(String id, Session session) {
        SESSIONS.put(id, session);
        GROUPS.computeIfAbsent(
                String.valueOf(id.hashCode() % CHANNEL_NUM),
                k -> new ConcurrentHashMap<>(16)
        ).put(id, session);
    }

    /**
     * 卸载会话
     *
     * @param id 会话编号
     */
    public static void unregister(String id) {
        SESSIONS.remove(id);
        final Map<String, Session> data = GROUPS.get(
                String.valueOf(id.hashCode() % CHANNEL_NUM));
        if (data != null) {
            data.remove(id);
        }
    }

    /**
     * 读取会话
     *
     * @param id 会话编号
     * @return 会话对象
     */
    @SuppressWarnings("ALL")
    public static Session get(String id) {
        return SESSIONS.get(id);
    }

    /**
     * 读取全部会话
     *
     * @return 全部会话对象
     */
    public static Iterator<Session> all() {
        return SESSIONS.values().iterator();
    }

    /**
     * 推送消息
     *
     * @param filter  过滤器
     * @param name    服务名称
     * @param id      消息编号
     * @param type    消息类型
     * @param content 消息内容
     */
    public static void push(Function<User, Boolean> filter, String name, String id, String type, String content) {
        final List<List<Session>> list = new ArrayList<>();
        GROUPS.forEach((k, v) -> list.add(new ArrayList<>(v.values())));
        for (final List<Session> sessions : list) {
            if (sessions != null && !sessions.isEmpty()) {
                submit(sessions, filter, name, id, JsonUtil.toJson(new HashMap<>() {{
                    put("id", id);
                    put("type", type);
                    put("content", content);
                }}));
            }
        }
    }

    /**
     * 提交任务
     *
     * @param channels 频道对象
     * @param filter   过滤器对象
     * @param name     服务名称
     * @param id       消息编号
     * @param content  消息内容
     */
    private static void submit(List<Session> channels, Function<User, Boolean> filter, String name, String id, String content) {
        EXECUTOR.submit(() -> {
            for (final Session session : channels) {
                if (name.equalsIgnoreCase(session.getName())) {
                    final Boolean result = filter.apply(session.getUser());
                    if (result != null && result) {
                        session.push(id, content);
                    }
                }
            }
        });
    }

}
