package club.p6e.coat.sse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 心跳
 *
 * @author lidashuang
 * @version 1.0
 */
public final class Heartbeat {

    /**
     * 轮训间隔时间
     */
    private static final long INTERVAL = 50;

    /**
     * 心跳的线程
     */
    private static ScheduledExecutorService EXECUTOR = null;

    /**
     * 初始化
     */
    public synchronized static void init() {
        if (EXECUTOR == null) {
            EXECUTOR = new ScheduledThreadPoolExecutor(1, r ->
                    new Thread(r, "P6E-SSE-HEARTBEAT-THREAD-" + r.hashCode()));
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
                final Iterator<Session> iterator = SessionManager.all();
                while (iterator.hasNext()) {
                    try {
                        iterator.next().refresh();
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
