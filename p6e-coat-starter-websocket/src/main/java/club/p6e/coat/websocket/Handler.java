package club.p6e.coat.websocket;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.PingWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PongWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 处理器
 *
 * @author lidashuang
 * @version 1.0
 */
final class Handler implements ChannelInboundHandler {

    private static final String LOGIN_CONTENT = "{\"type\":\"login\"}";
    private static final String LOGOUT_CONTENT = "{\"type\":\"logout\"}";
    /**
     * 注入日志对象
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(Handler.class);

    /**
     * 客户端 ID
     */
    private final String id;

    private final Auth<?> auth;

    private Session<?> session;

    /**
     * 构造方法初始化
     */
    public Handler(Auth<?> auth) {
        this.id = GeneratorUtil.uuid() + GeneratorUtil.random();
        this.auth = auth;
    }

    @Override
    public void channelRegistered(ChannelHandlerContext channelHandlerContext) {
        LOGGER.debug("[ " + id + " ] ==> channelRegistered");
    }

    @Override
    public void channelUnregistered(ChannelHandlerContext channelHandlerContext) {
        LOGGER.debug("[ " + id + " ] ==> channelUnregistered");
    }

    @Override
    public void channelActive(ChannelHandlerContext channelHandlerContext) {
        LOGGER.debug("[ " + id + " ] ==> channelActive");
    }

    @Override
    public void channelInactive(ChannelHandlerContext channelHandlerContext) {
        LOGGER.debug("[ " + id + " ] ==> channelInactive");
    }

    @Override
    public void channelRead(ChannelHandlerContext channelHandlerContext, Object o) {
        LOGGER.debug("[ " + id + " ] ==> channelRead, msg: " + o.getClass());
        if (o instanceof final TextWebSocketFrame textWebSocketFrame) {
            final String text = textWebSocketFrame.text();
            if (session != null && Heartbeat.CONTENT.equalsIgnoreCase(text)) {
                session.refresh();
                channelHandlerContext.writeAndFlush(new TextWebSocketFrame(Heartbeat.CONTENT));
            }
        } else if (o instanceof PingWebSocketFrame) {
            channelHandlerContext.writeAndFlush(new PongWebSocketFrame());
        } else if (o instanceof PongWebSocketFrame) {
            channelHandlerContext.writeAndFlush(new PingWebSocketFrame());
        }
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext channelHandlerContext) {
        LOGGER.debug("[ " + id + " ] ==> channelReadComplete");
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext channelHandlerContext, Object o) {
        LOGGER.debug("[ " + id + " ] ==> userEventTriggered, msg: " + o.getClass());
        if (o instanceof final WebSocketServerProtocolHandler.HandshakeComplete complete) {
            session = auth.validate(complete.requestUri(), channelHandlerContext);
            if (session != null) {
                SessionManager.register(id, session);
                channelHandlerContext.writeAndFlush(new TextWebSocketFrame(LOGIN_CONTENT));
                return;
            }
        }
        channelHandlerContext.close();
    }

    @Override
    public void channelWritabilityChanged(ChannelHandlerContext channelHandlerContext) {
        LOGGER.debug("[ " + id + " ] ==> channelWritabilityChanged");
    }

    @Override
    public void handlerAdded(ChannelHandlerContext channelHandlerContext) {
        LOGGER.debug("[ " + id + " ] ==> handlerAdded");
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext channelHandlerContext) {
        LOGGER.debug("[ " + id + " ] ==> handlerRemoved");
        SessionManager.unregister(id);
        channelHandlerContext.writeAndFlush(new TextWebSocketFrame(LOGOUT_CONTENT));
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext channelHandlerContext, Throwable throwable) {
        LOGGER.error("[ " + id + " ] ==> exceptionCaught " + throwable.getMessage());
        channelHandlerContext.close();
    }

}