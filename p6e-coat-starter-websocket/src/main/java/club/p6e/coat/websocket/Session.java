package club.p6e.coat.websocket;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;

/**
 * 会话
 *
 * @author lidashuang
 * @version 1.0
 */
@Getter
public final class Session {

    private static final Logger LOGGER = LoggerFactory.getLogger(Session.class);
    /**
     * 消息类型
     */
    private final Type type;
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
     * @param type    消息类型
     * @param user    用户对象
     * @param context 上下文对象
     */
    public Session(String name, Type type, User user, ChannelHandlerContext context) {
        this.name = name;
        this.type = type;
        this.user = user;
        this.context = context;
        this.date = System.currentTimeMillis();
    }

    /**
     * 刷新时间
     */
    public void refresh() {
        this.date = System.currentTimeMillis();
    }

    /**
     * 关闭会话
     */
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
    public void push(Object data) {
        if (context != null && !context.isRemoved()) {
            if (type == Type.TEXT && data instanceof String content) {
                LOGGER.info("[ SESSION PUSH TEST MESSAGE ] >>> {}", content);
                context.writeAndFlush(new TextWebSocketFrame(content));
            }
            if (type == Type.BINARY && data instanceof byte[] bytes) {
                LOGGER.info("[ SESSION PUSH BINARY MESSAGE ] >>> {}", Collections.singletonList(bytes));
                context.writeAndFlush(new BinaryWebSocketFrame(Unpooled.wrappedBuffer(bytes)));
            }
        }
    }

    /**
     * 类型
     */
    public enum Type {
        /**
         * 文本
         */
        TEXT,
        /**
         * 字节码
         */
        BINARY
    }

}
