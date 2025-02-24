package club.p6e.coat.sse;

import club.p6e.coat.common.context.ResultContext;
import club.p6e.coat.common.utils.GeneratorUtil;
import club.p6e.coat.common.utils.JsonUtil;
import club.p6e.coat.common.utils.SpringUtil;
import club.p6e.coat.sse.controller.Controller;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandler;
import io.netty.handler.codec.http.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

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
        LOGGER.debug("[ {} ] ==> channelRead, msg: {}", id, o.getClass());
        if (o instanceof FullHttpRequest request) {
            if (HttpUtil.is100ContinueExpected(request)) {
                context.write(new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.CONTINUE));
            }
            final String path = Controller.getPath(request.uri()).replaceAll("/", ".");
            System.out.println("11111 >> " + Controller.getPath(request.uri()));
            System.out.println("11111 >> " + Controller.getVoucher(request.uri()));
            final AuthService auth = SpringUtil.getBean(AuthService.class);
            final User user = auth.validate(path, Controller.getVoucher(request.uri()));
            if (!channels.contains(path) || user == null) {
                final HttpResponse response = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
                response.headers().set(HttpHeaderNames.CONTENT_TYPE, "application/json; charset=utf-8");
                context.write(response);
                if (user == null) {
                    context.write(Unpooled.copiedBuffer(JsonUtil.toJson(
                            ResultContext.build(500, "AUTH_ERROR", "AUTH_ERROR")
                    ).getBytes(StandardCharsets.UTF_8)));
                } else {
                    context.write(Unpooled.copiedBuffer(JsonUtil.toJson(
                            ResultContext.build(500, "CHANNEL_ERROR", "CHANNEL_ERROR")
                    ).getBytes(StandardCharsets.UTF_8)));
                }
                context.flush();
                context.close();
                return;
            }
            final Session session = new Session(path, user, context);
            final HttpResponse response = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
            response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/event-stream; charset=utf-8");
            response.headers().set(HttpHeaderNames.CACHE_CONTROL, "no-cache");
            response.headers().set(HttpHeaderNames.CONNECTION, "keep-alive");
            SessionManager.register(context.name(), session);
            context.write(response);
            context.executor().scheduleAtFixedRate(session::refresh, 0, 50, TimeUnit.SECONDS);
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
        LOGGER.debug("[ {} ] ==> handlerRemoved", id);
        SessionManager.unregister(context.name());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext context, Throwable throwable) {
        LOGGER.error("[ {} ] ==> exceptionCaught {}", id, throwable.getMessage());
        context.close();
    }

}
