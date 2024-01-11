package club.p6e.coat.websocket.auth;

import club.p6e.coat.websocket.User;
import jakarta.servlet.http.HttpServletRequest;

/**
 * 认证接口
 *
 * @author lidashuang
 * @version 1.0
 */
public interface AuthWebService {

    /**
     * 颁发令牌
     *
     * @param request 请求对象
     * @return 颁发的令牌
     */
    String award(HttpServletRequest request);

    /**
     * 验证令牌
     *
     * @param voucher 请求的令牌
     * @return 验证的结果
     */
    User validate(String voucher);

}
