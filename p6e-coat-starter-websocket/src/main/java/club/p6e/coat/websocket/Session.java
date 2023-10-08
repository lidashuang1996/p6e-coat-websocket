package club.p6e.coat.websocket;

import io.netty.channel.ChannelHandlerContext;
import lombok.Getter;

/**
 * @author lidashuang
 * @version 1.0
 */
@Getter
public class Session<T extends User> {

    private long date;
    private final T user;
    private final ChannelHandlerContext context;

    public Session(ChannelHandlerContext context, T user) {
        this.user = user;
        this.context = context;
        this.date = System.currentTimeMillis();
    }

    public void refresh() {
        this.date = System.currentTimeMillis();
    }

    public void close() {
        if (context != null && !context.isRemoved()) {
            context.close();
        }
    }

    public void push(String content) {
        if (context != null && !context.isRemoved()) {
            context.writeAndFlush(content);
        }
    }

}
