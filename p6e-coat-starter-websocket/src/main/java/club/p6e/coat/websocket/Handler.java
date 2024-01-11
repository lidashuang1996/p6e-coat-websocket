package club.p6e.coat.websocket;

import club.p6e.coat.common.utils.GeneratorUtil;
import club.p6e.coat.websocket.auth.AuthService;
import club.p6e.coat.websocket.controller.Controller;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

/**
 * 处理器
 *
 * @author lidashuang
 * @version 1.0
 */
final class Handler implements ChannelInboundHandler {

    /**
     * 登录成功写入文本内容
     */
    private static final String LOGIN_CONTENT_TEXT = "{\"type\":\"login\"}";

    /**
     * 登录成功写入字节码内容
     */
    private static final byte[] LOGIN_CONTENT_BYTES = new byte[]{
            16, 0, 0, 0, 16, 0, 1, 0, 0, 0, 0, 0, 2, 0, 0, 0
    };

    /**
     * 登出成功写入本内容
     */
    private static final String LOGOUT_CONTENT_TEXT = "{\"type\":\"logout\"}";

    /**
     * 登出成功写入字节码内容
     */
    private static final byte[] LOGOUT_CONTENT_BYTES = new byte[]{
            16, 0, 0, 0, 16, 0, 1, 0, 0, 0, 0, 0, 6, 0, 0, 0
    };

    /**
     * 注入日志对象
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(Handler.class);

    /**
     * 客户端 ID
     */
    private final String id;

    /**
     * 认证对象
     */
    private final AuthService auth;

    /**
     * 服务名称
     */
    private final String name;

    /**
     * 服务类型
     */
    private final String type;

    /**
     * 会话对象
     */
    private Session session;

    /**
     * 构造方法初始化
     *
     * @param auth 认证服务
     * @param name 服务名称
     * @param type 服务类型
     */
    public Handler(AuthService auth, String name, String type) {
        this.id = GeneratorUtil.uuid() + GeneratorUtil.random();
        this.auth = auth;
        this.name = name;
        this.type = type;
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
            if (session != null && Heartbeat.CONTENT_TEXT.equalsIgnoreCase(text)) {
                session.refresh();
                channelHandlerContext.writeAndFlush(new TextWebSocketFrame(Heartbeat.CONTENT_TEXT));
            }
        } else if (o instanceof final BinaryWebSocketFrame binaryWebSocketFrame) {
            final ByteBuf byteBuf = binaryWebSocketFrame.content();
            try {
                final int readableBytesLength = byteBuf.readableBytes();
                if (session != null && Heartbeat.CONTENT_BYTES.length == readableBytesLength) {
                    final byte[] readableByteArray = new byte[readableBytesLength];
                    byteBuf.readBytes(readableByteArray);
                    if (Arrays.equals(Heartbeat.CONTENT_BYTES, readableByteArray)) {
                        session.refresh();
                        channelHandlerContext.writeAndFlush(
                                new BinaryWebSocketFrame(Unpooled.wrappedBuffer(Heartbeat.CONTENT_BYTES)));
                    }
                }
            } finally {
                byteBuf.release();
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
            final User user = auth.validate(Controller.getVoucher(complete.requestUri()));
            if (user == null) {
                channelHandlerContext.close();
            } else {
                if (Session.Type.TEXT.name().equalsIgnoreCase(type)) {
                    session = new Session(name, Session.Type.TEXT, user, channelHandlerContext);
                    SessionManager.register(id, session);
                    channelHandlerContext.writeAndFlush(new TextWebSocketFrame(LOGIN_CONTENT_TEXT));
                } else if (Session.Type.BINARY.name().equalsIgnoreCase(type)) {
                    session = new Session(name, Session.Type.BINARY, user, channelHandlerContext);
                    SessionManager.register(id, session);
                    channelHandlerContext.writeAndFlush(
                            new BinaryWebSocketFrame(Unpooled.wrappedBuffer(LOGIN_CONTENT_BYTES)));
                }
            }
        }
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
        if (Session.Type.TEXT.name().equalsIgnoreCase(type)) {
            channelHandlerContext.writeAndFlush(new TextWebSocketFrame(LOGOUT_CONTENT_TEXT));
        } else if (Session.Type.BINARY.name().equalsIgnoreCase(type)) {
            channelHandlerContext.writeAndFlush(
                    new BinaryWebSocketFrame(Unpooled.wrappedBuffer(LOGOUT_CONTENT_BYTES)));
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext channelHandlerContext, Throwable throwable) {
        LOGGER.error("[ " + id + " ] ==> exceptionCaught " + throwable.getMessage());
        channelHandlerContext.close();
    }

}
