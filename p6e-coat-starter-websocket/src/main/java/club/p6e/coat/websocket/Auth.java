package club.p6e.coat.websocket;

import io.netty.channel.ChannelHandlerContext;
import jakarta.servlet.http.HttpServletRequest;

/**
 * @author lidashuang
 * @version 1.0
 */
public interface Auth<T extends User> {

    public String award(HttpServletRequest request);

    public Session<T> validate(String uri, ChannelHandlerContext context);

}
