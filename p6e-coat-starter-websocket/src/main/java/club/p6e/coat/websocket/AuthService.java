package club.p6e.coat.websocket;

/**
 * 认证接口
 *
 * @author lidashuang
 * @version 1.0
 */
public interface AuthService {

    /**
     * 验证令牌
     *
     * @param voucher 请求的令牌
     * @return 验证的结果
     */
    User validate(String voucher);

}
