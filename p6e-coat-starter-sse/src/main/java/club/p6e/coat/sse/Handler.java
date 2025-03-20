package club.p6e.coat.sse;

import club.p6e.coat.common.context.ResultContext;
import club.p6e.coat.common.utils.GeneratorUtil;
import club.p6e.coat.common.utils.JsonUtil;
import club.p6e.coat.common.utils.SpringUtil;
import club.p6e.coat.sse.controller.Controller;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandler;
import io.netty.handler.codec.http.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * 处理器
 *
 * @author lidashuang
 * @version 1.0
 */
final class Handler implements ChannelInboundHandler {

    /**
     * 注入日志对象
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(Handler.class);

    /**
     * 配置
     */
    private final List<String> channels = new ArrayList<>();

    /**
     * 客户端 ID
     */
    private final String id;

    /**
     * 构造方法初始化
     */
    public Handler() {
        this.id = GeneratorUtil.uuid() + GeneratorUtil.random();
    }

    /**
     * 设置频道
     *
     * @param channels 频道内容
     */
    public void setChannels(List<String> channels) {
        this.channels.clear();
        this.channels.addAll(channels);
    }

    @Override
    public void channelRegistered(ChannelHandlerContext context) {
        LOGGER.debug("[ {} ] ==> channelRegistered", id);
    }

    @Override
    public void channelUnregistered(ChannelHandlerContext context) {
        LOGGER.debug("[ {} ] ==> channelUnregistered", id);
    }

    @Override
    public void channelActive(ChannelHandlerContext context) {
        LOGGER.debug("[ {} ] ==> channelActive", id);
    }

    @Override
    public void channelInactive(ChannelHandlerContext context) {
        LOGGER.debug("[ {} ] ==> channelInactive", id);
    }

    @Override
    public void channelRead(ChannelHandlerContext context, Object o) {
        LOGGER.info("[ {} ] ==> channelRead, msg: {}", id, o.getClass());
        if (o instanceof FullHttpRequest request) {
            final FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
            final String origin = request.headers().get(HttpHeaderNames.ORIGIN);
            response.headers().set(HttpHeaderNames.ACCESS_CONTROL_ALLOW_ORIGIN, origin == null ? "*" : origin);
            response.headers().set(HttpHeaderNames.ACCESS_CONTROL_ALLOW_METHODS, "GET,POST,DELETE,PUT,OPTIONS");
            response.headers().set(HttpHeaderNames.ACCESS_CONTROL_ALLOW_HEADERS, "Content-Type");
            response.headers().set(HttpHeaderNames.ACCESS_CONTROL_MAX_AGE, "3600");
            if (HttpMethod.OPTIONS.equals(request.method())) {
                context.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
            } else {
                if (HttpUtil.is100ContinueExpected(request)) {
                    context.write(new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.CONTINUE));
                }
                final String path = Controller.getPath(request.uri()).replaceAll("/", ".");
                final String[] pathSplitContent = (path.startsWith(".") ? path.substring(1) : path).split("/");
                final String name = String.join(".", pathSplitContent);
                final AuthService auth = SpringUtil.getBean(AuthService.class);
                final User user = auth.validate(name, Controller.getVoucher(request.uri()));
                if (!channels.contains(name) || user == null) {
                    if (user == null) {
                        response.content().writeBytes(context.alloc().buffer().writeBytes(JsonUtil.toJson(
                                ResultContext.build(500, "AUTH_ERROR", "AUTH_ERROR")
                        ).getBytes(StandardCharsets.UTF_8)));
                    } else {
                        response.content().writeBytes(context.alloc().buffer().writeBytes(JsonUtil.toJson(
                                ResultContext.build(500, "CHANNEL_ERROR", "CHANNEL_ERROR")
                        ).getBytes(StandardCharsets.UTF_8)));
                    }
                    context.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
                    context.close();
                    return;
                }
                response.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
                response.headers().set(HttpHeaderNames.CACHE_CONTROL, HttpHeaderValues.NO_CACHE);
                response.headers().set(HttpHeaderNames.CONTENT_TYPE, HttpHeaderValues.TEXT_EVENT_STREAM);
                final Session session = new Session(name, user, context);
                SessionManager.register(context.channel().id().toString(), session);
                context.writeAndFlush(response).addListener((ChannelFutureListener) cf -> {
                    if (cf.isSuccess()) {
                        session.refresh();
                    }
                });
            }
        }
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext channelHandlerContext) {
        LOGGER.debug("[ {} ] ==> channelReadComplete", id);
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext context, Object o) {
        LOGGER.debug("[ {} ] ==> userEventTriggered, msg: {}", id, o.getClass());
    }

    @Override
    public void channelWritabilityChanged(ChannelHandlerContext context) {
        LOGGER.debug("[ {} ] ==> channelWritabilityChanged", id);
    }

    @Override
    public void handlerAdded(ChannelHandlerContext context) {
        LOGGER.debug("[ {} ] ==> handlerAdded", id);
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext context) {
        LOGGER.info("[ {} ] ==> handlerRemoved", id);
        SessionManager.unregister(context.channel().id().toString());
        if (context.channel().isActive()) {
            context.channel().close();
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext context, Throwable throwable) {
        LOGGER.error("[ {} ] ==> exceptionCaught {}", id, throwable.getMessage());
        context.close();
    }

}
