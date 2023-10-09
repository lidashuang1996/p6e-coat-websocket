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
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * WebSocket Main
 *
 * @author lidashuang
 * @version 1.0
 */
@Component
@ConditionalOnMissingBean(
        value = WebSocketMain.class,
        ignored = WebSocketMain.class
)
public class WebSocketMain {

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
    }

    /**
     * 启动线程池大小
     */
    public static int THREAD_POOL_LENGTH = 15;

    /**
     * 启动配置信息
     */
    public static List<Config> CONFIGS = new ArrayList<>();

    /**
     * 注入日志对象
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(WebSocketMain.class);

    /**
     * 认证对象
     */
    private final Auth auth;

    /**
     * 构造方法初始化
     *
     * @param auth 认证对象
     */
    public WebSocketMain(Auth auth) {
        this.auth = auth;
        for (final Config config : CONFIGS) {
            init(config.getPort(), config.getName(), THREAD_POOL_LENGTH);
        }
    }

    /**
     * 初始化方法
     *
     * @param port             端口
     * @param name             服务名称
     * @param threadPoolLength 线程池大小
     */
    public void init(int port, String name, int threadPoolLength) {
        init(port, auth, name, threadPoolLength);
    }

    /**
     * 推送消息
     *
     * @param filter  过滤器对象
     * @param group   消息组
     * @param id      消息编号
     * @param type    消息类型
     * @param content 消息内容
     */
    public void push(Function<User, Boolean> filter, String group, String id, String type, String content) {
        SessionManager.push(filter, group, id, type, content);
    }

    /**
     * 初始化方法
     *
     * @param port             启动的端口
     * @param auth             认证对象
     * @param name             服务名称
     * @param threadPoolLength 启动的处理消息的线程池大小
     */
    private static void init(int port, Auth auth, String name, int threadPoolLength) {
        Heartbeat.init();
        SessionManager.init(threadPoolLength);
        new Thread() {
            @Override
            public void run() {
                super.run();
                netty(port, auth, name);
            }
        }.start();
    }

    /**
     * Netty WebSocket 服务启动
     *
     * @param port 启动的端口
     * @param auth 认证对象
     * @param name 服务名称
     */
    private static void netty(int port, Auth auth, String name) {
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
                    channel.pipeline().addLast(new Handler(auth, name));
                }
            });
            final Channel channel = bootstrap.bind(port).sync().channel();
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                boss.shutdownGracefully();
                work.shutdownGracefully();
            }));
            LOGGER.info("[ WEBSOCKET SERVICE ] ==> START SUCCESSFULLY... BIND ( " + port + " )");
            channel.closeFuture().sync();
        } catch (Exception e) {
            LOGGER.error("[ WEBSOCKET SERVICE ]", e);
        } finally {
            boss.shutdownGracefully();
            work.shutdownGracefully();
        }
    }

}
