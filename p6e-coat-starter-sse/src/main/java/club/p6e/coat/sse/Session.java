package club.p6e.coat.sse;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import lombok.Getter;

import java.nio.charset.StandardCharsets;

/**
 * 会话
 *
 * @author lidashuang
 * @version 1.0
 */
@Getter
public final class Session {

    /**
     * 用户对象
     */
    private final User user;

    /**
     * 服务名称
     */
    private final String name;

    /**
     * 上下文对象
     */
    private final ChannelHandlerContext context;

    /**
     * 时间
     */
    private volatile long date;

    /**
     * 构造方法初始化
     *
     * @param name    服务名称
     * @param user    用户对象
     * @param context 上下文对象
     */
    public Session(String name, User user, ChannelHandlerContext context) {
        this.name = name;
        this.user = user;
        this.context = context;
        this.date = System.currentTimeMillis();
    }

    /**
     * 刷新
     */
    public void refresh() {
        push("{\"type\":\"heartbeat \"}");
        this.date = System.currentTimeMillis();
    }

    /**
     * 关闭
     */
    @SuppressWarnings("ALL")
    public void close() {
        if (context != null && !context.isRemoved()) {
            context.close();
        }
    }

    /**
     * 推送消息
     *
     * @param data 消息内容
     */
    public void push(String data) {
        if (context != null && context.channel().isOpen()) {
            context.channel().write(Unpooled.copiedBuffer(data, StandardCharsets.UTF_8));
        }
    }

}
