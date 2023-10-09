package club.p6e.coat.websocket;

import io.netty.channel.ChannelHandlerContext;
import jakarta.servlet.http.HttpServletRequest;

/**
 * 认证接口
 *
 * @author lidashuang
 * @version 1.0
 */
public interface Auth {

    /**
     * 颁发令牌
     *
     * @param request 请求对象
     * @return 颁发的令牌
     */
    public String award(HttpServletRequest request);

    /**
     * 验证令牌
     *
     * @param uri 请求的 URL
     * @return 验证的结果
     */
    public User validate(String uri);

}
