package club.p6e.coat.sse;

import club.p6e.coat.common.utils.GeneratorUtil;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;

/**
 * 认证接口
 *
 * @author lidashuang
 * @version 1.0
 */
@Component
@ConditionalOnMissingBean(
        value = AuthServiceImpl.class,
        ignored = AuthServiceImpl.class
)
public class AuthServiceImpl implements AuthService {

    @Override
    public User validate(String channel, String voucher) {
        return GeneratorUtil::uuid;
    }

}
