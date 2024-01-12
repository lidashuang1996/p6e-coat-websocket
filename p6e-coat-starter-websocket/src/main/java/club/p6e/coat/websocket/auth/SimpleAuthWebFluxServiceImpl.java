package club.p6e.coat.websocket.auth;

import club.p6e.coat.common.utils.GeneratorUtil;
import club.p6e.coat.websocket.User;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * @author lidashuang
 * @version 1.0
 */
@Component
@ConditionalOnMissingBean(
        value = AuthWebFluxService.class,
        ignored = SimpleAuthWebFluxServiceImpl.class
)
@ConditionalOnClass(name = "org.springframework.web.reactive.package-info")
public class SimpleAuthWebFluxServiceImpl implements AuthWebFluxService {

    @Override
    public Mono<String> award(ServerWebExchange exchange) {
        return Mono.just(GeneratorUtil.uuid());
    }

    @Override
    public Mono<User> validate(String voucher) {
        return Mono.just(() -> voucher);
    }

}
