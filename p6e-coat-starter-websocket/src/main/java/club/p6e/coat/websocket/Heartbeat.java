package club.p6e.coat.websocket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 心跳
 *
 * @author lidashuang
 * @version 1.0
 */
final class Heartbeat {

    /**
     * 心跳的文本
     */
    public static final String CONTENT_TEXT = "{\"type\":\"heartbeat\"}";

    /**
     * 心跳的字节码
     */
    public static final byte[] CONTENT_BYTES = new byte[]{
            16, 0, 0, 0, 16, 0, 1, 0, 0, 0, 0, 0, 4, 0, 0, 0
    };

    /**
     * 轮训间隔时间
     */
    private static final long INTERVAL = 60;

    /**
     * 心跳的线程
     */
    private static ScheduledExecutorService EXECUTOR = null;


    /**
     * 初始化
     */
    public synchronized static void init() {
        if (EXECUTOR == null) {
            EXECUTOR = Executors.newScheduledThreadPool(1);
            EXECUTOR.schedule(new Task(), INTERVAL, TimeUnit.SECONDS);
        }
    }

    /**
     * 任务
     */
    private static class Task implements Runnable {

        /**
         * 注入日志对象
         */
        private static final Logger LOGGER = LoggerFactory.getLogger(Task.class);

        @Override
        public void run() {
            try {
                final long now = System.currentTimeMillis();
                final Iterator<Session> iterator = SessionManager.all();
                while (iterator.hasNext()) {
                    try {
                        final Session session = iterator.next();
                        if (now - session.getDate() > INTERVAL * 1000) {
                            session.close();
                        }
                    } catch (Exception e) {
                        LOGGER.error(e.getMessage());
                    }
                }
            } catch (Exception e) {
                LOGGER.error(e.getMessage());
            } finally {
                EXECUTOR.schedule(new Task(), INTERVAL, TimeUnit.SECONDS);
            }
        }
    }

}
