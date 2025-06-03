package club.p6e.coat.websocket;

import club.p6e.coat.common.utils.JsonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    private static final Logger LOGGER = LoggerFactory.getLogger(SessionManager.class);
    /**
     * 会话对象
     */
    private static final Map<String, Session> SESSIONS = new ConcurrentHashMap<>();
    /**
     * 频道对象
     */
    private static final Map<String, Map<String, Session>> CHANNELS = new ConcurrentHashMap<>();
    /**
     * 频道（线程）数量
     */
    private static int CHANNEL_NUM = 15;
    /**
     * 线程池对象
     */
    private static ScheduledThreadPoolExecutor EXECUTOR = null;

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
                    new Thread(r, "P6E-WS-SESSION-MANAGER-THREAD-" + r.hashCode()));
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
        synchronized (SessionManager.class) {
            SESSIONS.put(id, session);
            final String channel = String.valueOf(Math.abs(id.hashCode() % CHANNEL_NUM));
            CHANNELS.computeIfAbsent(
                    channel,
                    k -> new ConcurrentHashMap<>(16)
            ).put(id, session);
            LOGGER.info("[ SESSION REGISTER ] >> {}%{}={} >>> {}", id, CHANNEL_NUM, channel, CHANNELS.get(channel));
        }
    }

    /**
     * 卸载会话
     *
     * @param id 会话编号
     */
    public static void unregister(String id) {
        synchronized (SessionManager.class) {
            SESSIONS.remove(id);
            final String channel = String.valueOf(Math.abs(id.hashCode() % CHANNEL_NUM));
            final Map<String, Session> data = CHANNELS.get(channel);
            if (data != null) {
                data.remove(id);
            }
            LOGGER.info("[ SESSION UNREGISTER ] >> {}%{}={} >>> {}", id, CHANNEL_NUM, channel, CHANNELS.get(channel));
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
     * 读取全部会话
     *
     * @return 全部会话对象
     */
    public static Set<String> keys() {
        return SESSIONS.keySet();
    }

    /**
     * 读取频道全部会话
     *
     * @return 频道全部会话
     */
    private static List<List<Session>> getChannel() {
        final List<List<Session>> list = new ArrayList<>();
        CHANNELS.forEach((k, v) -> {
            if (!v.isEmpty()) {
                final List<Session> sessions = new ArrayList<>(v.values());
                list.add(sessions);
                LOGGER.info("[ SESSION CHANNEL ] {} >>> {}", k, sessions);
            }
        });
        return list;
    }

    /**
     * 推送消息
     *
     * @param filter 过滤器对象
     * @param name   服务名称
     * @param bytes  消息内容
     */
    public static void pushBinary(Function<User, Boolean> filter, String name, byte[] bytes) {
        final List<List<Session>> list = getChannel();
        for (final List<Session> channels : list) {
            if (channels != null && !channels.isEmpty()) {
                LOGGER.info("[ PUSH BINARY SESSION CHANNEL ] {} >>> {}", name, Collections.singletonList(bytes));
                submit(channels, filter, name, bytes);
            }
        }
    }

    /**
     * 推送消息
     *
     * @param filter  过滤器对象
     * @param name    服务名称
     * @param id      消息编号
     * @param type    消息类型
     * @param content 消息内容
     */
    public static void pushText(Function<User, Boolean> filter, String name, String id, String type, String content) {
        final List<List<Session>> list = getChannel();
        final Map<String, String> data = new HashMap<>();
        data.put("id", id);
        data.put("type", type);
        data.put("content", content);
        final String wc = JsonUtil.toJson(data);
        for (final List<Session> channels : list) {
            if (channels != null && !channels.isEmpty()) {
                LOGGER.info("[ PUSH TEXT SESSION CHANNEL ] {} >>> {}", name, wc);
                submit(channels, filter, name, wc);
            }
        }
    }

    /**
     * 提交任务
     *
     * @param channels 频道对象
     * @param filter   过滤器对象
     * @param name     服务名称
     * @param content  消息内容
     */
    private static void submit(List<Session> channels, Function<User, Boolean> filter, String name, Object content) {
        EXECUTOR.submit(() -> {
            for (final Session session : channels) {
                LOGGER.info("[ SUBMIT TASK EXECUTE ] >>> NAME CHECK >>> N:{}/SN:{} ? {}", name, session.getName(), name.equalsIgnoreCase(session.getName()));
                if (name.equalsIgnoreCase(session.getName())) {
                    final Boolean result = filter.apply(session.getUser());
                    LOGGER.info("[ SUBMIT TASK EXECUTE ] >>> FILTER RESULT >>> {}", result);
                    if (result != null && result) {
                        session.push(content);
                    }
                }
            }
        });
    }

}
