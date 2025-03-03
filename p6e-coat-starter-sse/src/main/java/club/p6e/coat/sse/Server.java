package club.p6e.coat.sse;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.Function;

/**
 * Server
 *
 * @author lidashuang
 * @version 1.0
 */
@Component
public final class Server {

    /**
     * 注入日志对象
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(Server.class);

    /**
     * 处理器对象
     */
    private final Handler handler = new Handler();

    private final EventLoopGroup bossGroup = new NioEventLoopGroup();
    private final EventLoopGroup workerGroup = new NioEventLoopGroup();

    @Getter
    private Integer port = 37100;

    @Getter
    private Integer threadPoolLength = 15;

    @Getter
    private ChannelFuture channelFuture;

    /**
     * 运行服务
     */
    public void run() {
        synchronized (this) {
            if (this.channelFuture != null) {
                this.channelFuture.channel().close();
                this.channelFuture = null;
            }
            Heartbeat.init();
            SessionManager.init(this.threadPoolLength);
            final ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(this.bossGroup, this.workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel ch) {
                            ch.pipeline().addLast(new HttpServerCodec());
                            ch.pipeline().addLast(new HttpObjectAggregator(1048576));
                            ch.pipeline().addLast(handler);
                        }
                    });
            this.channelFuture = bootstrap.bind(this.port);
            LOGGER.info("SERVER STARTED ON PORT >>> {}", this.port);
        }
    }

    /**
     * 设置端口信息
     *
     * @param port 端口信息
     */
    @SuppressWarnings("ALL")
    public synchronized void setPort(int port) {
        this.port = port;
    }

    /**
     * 设置频道信息
     *
     * @param channels 频道信息
     */
    @SuppressWarnings("ALL")
    public synchronized void setChannels(List<String> channels) {
        if (channels != null && !channels.isEmpty()) {
            this.handler.setChannels(channels);
        }
    }

    /**
     * 设置线程池长度
     *
     * @param threadPoolLength 线程池长度
     */
    @SuppressWarnings("ALL")
    public synchronized void setThreadPoolLength(Integer threadPoolLength) {
        this.threadPoolLength = threadPoolLength;
        SessionManager.init(this.threadPoolLength);
    }

    /**
     * 设置线程池长度
     *
     * @param num 线程池长度
     */
    @SuppressWarnings("ALL")
    public synchronized void setThreadPoolLength(int num) {
        SessionManager.init(num);
    }

    /**
     * 重启
     */
    @SuppressWarnings("ALL")
    public void reset() {
        run();
    }

    /**
     * 关闭
     */
    @SuppressWarnings("ALL")
    public void close() {
        synchronized (this) {
            this.bossGroup.shutdownGracefully();
            this.workerGroup.shutdownGracefully();
            this.channelFuture.channel().close();
            this.channelFuture = null;
        }
    }

    /**
     * 消息推送
     *
     * @param filter  过滤器
     * @param name    服务名称
     * @param id      消息编号
     * @param type    消息类型
     * @param content 消息内容
     */
    public void push(Function<User, Boolean> filter, String name, String id, String type, String content) {
        SessionManager.push(filter, name, id, type, content);
    }

}
