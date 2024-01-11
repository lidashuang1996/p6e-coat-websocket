package club.p6e.coat.websocket.auth;

import club.p6e.coat.websocket.User;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * 认证接口
 *
 * @author lidashuang
 * @version 1.0
 */
public interface AuthWebFluxService {

    /**
     * 颁发令牌
     *
     * @param exchange 请求对象
     * @return 颁发的令牌
     */
    Mono<String> award(ServerWebExchange exchange);

    /**
     * 验证令牌
     *
     * @param voucher 请求的令牌
     * @return 验证的结果
     */
    Mono<User> validate(String voucher);

}
