package club.p6e.coat.websocket;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import lombok.Getter;

/**
 * 会话
 *
 * @author lidashuang
 * @version 1.0
 */
@Getter
public final class Session {

    /**
     * 时间
     */
    private long date;

    /**
     * 用户对象
     */
    private final User user;

    /**
     * 用户组
     */
    private final String group;

    /**
     * 上下文对象
     */
    private final ChannelHandlerContext context;

    /**
     * 构造方法初始化
     *
     * @param user    用户对象
     * @param group   用户组
     * @param context 上下文对象
     */
    public Session(User user, String group, ChannelHandlerContext context) {
        this.user = user;
        this.group = group;
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
     * @param content 消息内容
     */
    public void push(String content) {
        if (context != null && !context.isRemoved()) {
            context.writeAndFlush(new TextWebSocketFrame(content));
        }
    }

}
