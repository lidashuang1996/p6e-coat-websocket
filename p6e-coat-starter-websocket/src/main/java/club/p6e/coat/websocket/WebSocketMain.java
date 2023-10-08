package club.p6e.coat.websocket;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.function.Function;

/**
 *
 * @author lidashuang
 * @version 1.0
 */
@Component
public class WebSocketMain {

    /**
     * 注入日志对象
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(WebSocketMain.class);

    private final Auth<?> auth;

    public WebSocketMain(Auth<?> auth) {
        this.auth = auth;
    }

    /**
     * 推送消息
     *
     * @param id      消息ID
     * @param content 消息内容
     */
    public void push(Function<User, Boolean> filter, String id, String type, String content) {
        SessionManager.push(filter, id, type, content);
    }

    /**
     * 初始化方法
     *
     * @param port             启动的端口
     * @param threadPoolLength 启动的处理消息的线程池大小
     */
    private static void init(int port, int threadPoolLength) {
        Heartbeat.init();
        SessionManager.init(threadPoolLength);
        new Thread() {
            @Override
            public void run() {
                super.run();
                netty(port);
            }
        }.start();
    }

    /**
     * NETTY WebSocket 服务启动
     *
     * @param port 启动的端口
     */
    private static void netty(int port) {
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
                    channel.pipeline().addLast(new Handler(auth));
                }
            });
            final Channel channel = bootstrap.bind(port).sync().channel();
            // 返回与当前Java应用程序关联的运行时对象
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                boss.shutdownGracefully();
                work.shutdownGracefully();
            }));
            LOGGER.info("[ WEBSOCKET SERVICE ] ==> START SUCCESSFULLY... BIND ( " + port + " )");
            channel.closeFuture().sync();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            boss.shutdownGracefully();
            work.shutdownGracefully();
        }
    }

}
