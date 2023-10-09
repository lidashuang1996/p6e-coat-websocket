package club.p6e.coat.websocket;

import club.p6e.coat.common.utils.JsonUtil;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Function;

/**
 * 会话管理器
 *
 * @author lidashuang
 * @version 1.0
 */
final class SessionManager {

    /**
     * 频道（线程）数量
     */
    private static int CHANNEL_NUM;

    /**
     * 线程池对象
     */
    private static ScheduledExecutorService EXECUTOR = null;

    /**
     * 会话对象
     */
    private static final Map<String, Session> SESSIONS = new ConcurrentHashMap<>();

    /**
     * 频道对象
     */
    private static final Map<String, Map<String, Session>> CHANNELS = new ConcurrentHashMap<>();

    /**
     * 初始化方法
     *
     * @param num 频道（线程）数量
     */
    public static void init(int num) {
        CHANNEL_NUM = num;
        EXECUTOR = Executors.newScheduledThreadPool(num);
    }

    /**
     * 注册会话
     *
     * @param id      会话编号
     * @param session 会话对象
     */
    public static void register(String id, Session session) {
        SESSIONS.put(id, session);
        CHANNELS.computeIfAbsent(
                String.valueOf(id.hashCode() % CHANNEL_NUM),
                k -> new ConcurrentHashMap<>()
        ).put(id, session);
    }

    /**
     * 卸载会话
     *
     * @param id 会话编号
     */
    public static void unregister(String id) {
        SESSIONS.remove(id);
        final Map<String, Session> data = CHANNELS.get(
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
     * 读取频道全部会话
     *
     * @return 频道全部会话
     */
    private static List<List<Session>> getChannel() {
        final List<List<Session>> list = new ArrayList<>();
        CHANNELS.forEach((k, v) -> list.add(new ArrayList<>(v.values())));
        return list;
    }

    /**
     * 推送消息
     *
     * @param filter  过滤器对象
     * @param group   用户组
     * @param id      消息编号
     * @param type    消息类型
     * @param content 消息内容
     */
    public static void push(Function<User, Boolean> filter,
                            String group, String id, String type, String content) {
        final List<List<Session>> list = getChannel();
        final Map<String, String> data = new HashMap<>();
        data.put("id", id);
        data.put("type", type);
        data.put("content", content);
        final String wc = JsonUtil.toJson(data);
        for (final List<Session> channels : list) {
            if (channels != null && !channels.isEmpty()) {
                EXECUTOR.submit(() -> {
                    for (final Session session : channels) {
                        if (group.equalsIgnoreCase(session.getGroup())) {
                            final Boolean result = filter.apply(session.getUser());
                            if (result != null && result) {
                                session.push(wc);
                            }
                        }
                    }
                });
            }
        }
    }

}
