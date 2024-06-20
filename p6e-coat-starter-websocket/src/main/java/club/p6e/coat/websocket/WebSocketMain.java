package club.p6e.coat.websocket;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import lombok.Data;
import lombok.experimental.Accessors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * WebSocket Main
 *
 * @author lidashuang
 * @version 1.0
 */
@Component
public class WebSocketMain {

    /**
     * 客户端对象
     *
     * @param boss 服务端
     * @param work 工作端
     */
    public record Client(EventLoopGroup boss, EventLoopGroup work) implements Serializable {
    }

    /**
     * 启动配置信息
     */
    @Data
    @Accessors(chain = true)
    public static class Config implements Serializable {
        /**
         * 服务名称
         */
        private String name;

        /**
         * 服务端口
         */
        private Integer port;

        /**
         * 服务类型
         */
        private String type;
    }

    /**
     * 启动线程池大小
     */
    private int threadPoolLength = 15;

    /**
     * 客户端对象
     */
    private static final Map<String, Client> CLIENTS = new ConcurrentHashMap<>();

    /**
     * 注入日志对象
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(WebSocketMain.class);

    /**
     * 认证对象
     */
    private final AuthService auth;

    /**
     * 启动配置信息
     */
    private final List<Config> configs = new ArrayList<>();

    /**
     * 构造方法初始化
     *
     * @param auth 认证对象
     */
    public WebSocketMain(AuthService auth) {
        this.auth = auth;
    }

    /**
     * 重新根据配置文件初始化
     */
    @SuppressWarnings("ALL")
    public synchronized void reset() {
        if (configs.isEmpty()) {
            configs.add(new WebSocketMain.Config()
                    .setPort(9600)
                    .setType("TEXT")
                    .setName("DEFAULT")
            );
        }
        final List<Config> create = new ArrayList<>();
        final List<String> remove = new ArrayList<>(CLIENTS.keySet());
        init(threadPoolLength);
        for (final Config config : configs) {
            final String key = config.getName() + ":" + config.getType() + ":" + config.getPort();
            if (CLIENTS.get(key) == null) {
                create.add(config);
            } else {
                remove.remove(key);
            }
        }
        for (final String key : remove) {
            try {
                final Client client = CLIENTS.get(key);
                if (client != null) {
                    if (client.boss != null) {
                        client.boss.shutdownGracefully();
                    }
                    if (client.work != null) {
                        client.work.shutdownGracefully();
                    }
                }
            } catch (Exception e) {
                // ignore
            } finally {
                CLIENTS.remove(key);
            }
        }
        for (final Config config : create) {
            client(auth, config.getPort(), config.getName(), config.getType(), unused -> {
                notifyResource();
                return null;
            });
            waitResource();
        }
    }

    /**
     * 设置配置信息
     *
     * @param configs 配置信息
     */
    @SuppressWarnings("ALL")
    public synchronized void setConfig(List<Config> configs) {
        if (configs != null && !configs.isEmpty()) {
            this.configs.clear();
            this.configs.addAll(configs);
        }
    }

    /**
     * 设置线程池大小
     *
     * @param threadPoolLength 线程池大小
     */
    @SuppressWarnings("ALL")
    public synchronized void setThreadPoolLength(int threadPoolLength) {
        this.threadPoolLength = threadPoolLength;
    }

    private synchronized void waitResource() {
        try {
            wait();
        } catch (Exception e) {
            // ignore
        }
    }

    private synchronized void notifyResource() {
        try {
            notifyAll();
        } catch (Exception e) {
            // ignore
        }
    }

    /**
     * 推送消息
     *
     * @param filter 过滤器对象
     * @param name   消息组
     * @param bytes  消息内容
     */
    public void push(Function<User, Boolean> filter, String name, byte[] bytes) {
        SessionManager.pushBinary(filter, name, bytes);
    }

    /**
     * 推送消息
     *
     * @param filter  过滤器对象
     * @param name    消息组
     * @param id      消息编号
     * @param type    消息类型
     * @param content 消息内容
     */
    public void push(Function<User, Boolean> filter, String name, String id, String type, String content) {
        SessionManager.pushText(filter, name, id, type, content);
    }

    /**
     * 初始化方法
     *
     * @param threadPoolLength 启动的处理消息的线程池大小
     */
    private static void init(int threadPoolLength) {
        Heartbeat.init();
        SessionManager.init(threadPoolLength);
    }

    /**
     * 初始化方法
     *
     * @param auth     认证对象
     * @param port     启动的端口
     * @param name     服务名称
     * @param type     服务类型
     * @param callback 回调函数
     */
    @SuppressWarnings("ALL")
    private static void client(AuthService auth, int port, String name, String type, Function<Void, Void> callback) {
        new Thread() {
            @Override
            public void run() {
                super.run();
                netty(auth, port, name, type, callback);
            }
        }.start();
    }

    /**
     * Netty WebSocket 服务启动
     *
     * @param auth     认证对象
     * @param port     启动的端口
     * @param name     服务名称
     * @param type     服务类型
     * @param callback 回调函数
     */
    private static void netty(AuthService auth, int port, String name, String type, Function<Void, Void> callback) {
        if (CLIENTS.get(name) != null) {
            LOGGER.error("[ WEBSOCKET SERVICE ] ({} : {}) BIND ( {} ) ==> THERE ARE CHANNELS WITH THE SAME NAME.", name, type, port);
            return;
        }
        final EventLoopGroup boss = new NioEventLoopGroup();
        final EventLoopGroup work = new NioEventLoopGroup();
        try {
            final ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(boss, work);
            bootstrap.channel(NioServerSocketChannel.class);
            bootstrap.childHandler(new ChannelInitializer<>() {
                @Override
                protected void initChannel(Channel channel) {
                    // HTTP
                    channel.pipeline().addLast(new HttpServerCodec());
                    // WEBSOCKET
                    channel.pipeline().addLast(new WebSocketServerProtocolHandler(
                            "/ws", null, true,
                            65536, false, true));
                    // 自定义
                    channel.pipeline().addLast(new Handler(auth, name, type));
                }
            });
            final Channel channel = bootstrap.bind(port).sync().channel();
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                final Client client = CLIENTS.get(name + ":" + type + ":" + port);
                if (client != null) {
                    if (client.boss != null) {
                        client.boss.shutdownGracefully();
                    }
                    if (client.work != null) {
                        client.work.shutdownGracefully();
                    }
                }
            }));
            CLIENTS.put(name + ":" + type + ":" + port, new Client(boss, work));
            LOGGER.info("[ WEBSOCKET SERVICE ] ({} : {}) ==> START SUCCESSFULLY... BIND ( {} )", name, type, port);
            callback.apply(null);
            channel.closeFuture().sync();
        } catch (Exception e) {
            LOGGER.error("[ WEBSOCKET SERVICE ]", e);
        } finally {
            boss.shutdownGracefully();
            work.shutdownGracefully();
        }
    }

}
