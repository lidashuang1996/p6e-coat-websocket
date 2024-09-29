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
     * @param channel 通道名称
     * @param voucher 请求令牌
     * @return 验证的结果
     */
    User validate(String channel, String voucher);

}
