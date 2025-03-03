package club.p6e.coat.sse;

import club.p6e.coat.common.utils.GeneratorUtil;
import club.p6e.coat.common.utils.JsonUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import lombok.Getter;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;

/**
 * 会话
 *
 * @author lidashuang
 * @version 1.0
 */
@Getter
public final class Session {

    /**
     * 时间格式化对象
     */
    public static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

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
        push(null, JsonUtil.toJson(new HashMap<>() {{
            put("type", "heartbeat");
            put("data", String.valueOf(System.currentTimeMillis()));
        }}));
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
    public void push(String id, String data) {
        if (id == null) {
            id = DATE_TIME_FORMATTER.format(LocalDateTime.now()) + GeneratorUtil.uuid();
        }
        final String content = "id: " + id + "\ntype: message" + "\ndata: " + data + "\n\n";
        if (context != null && context.channel().isOpen()) {
            context.channel().writeAndFlush(
                    new DefaultFullHttpResponse(
                            HttpVersion.HTTP_1_1,
                            HttpResponseStatus.OK,
                            context.alloc().buffer().writeBytes(content.getBytes(StandardCharsets.UTF_8))
                    )
            );
        }
    }

}
