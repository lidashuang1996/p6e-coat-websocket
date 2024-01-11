package club.p6e.coat.websocket.auth;

import club.p6e.coat.websocket.User;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

/**
 * 认证的验证服务
 *
 * @author lidashuang
 * @version 1.0
 */
@Component
public class AuthService {

    /**
     * 注入应用上下文对象
     */
    private final ApplicationContext context;

    /**
     * 构造方法初始化
     *
     * @param context 应用上下文对象
     */
    public AuthService(ApplicationContext context) {
        this.context = context;
    }

    /**
     * 验证令牌
     *
     * @param voucher 请求的令牌
     * @return 验证的结果
     */
    public User validate(String voucher) {
        try {
            final AuthWebService service = context.getBean(AuthWebService.class);
            return service.validate(voucher);
        } catch (Exception e) {
            // ignore
        }
        try {
            final AuthWebFluxService service = context.getBean(AuthWebFluxService.class);
            return service.validate(voucher).block();
        } catch (Exception e) {
            // ignore
        }
        return null;
    }

}
