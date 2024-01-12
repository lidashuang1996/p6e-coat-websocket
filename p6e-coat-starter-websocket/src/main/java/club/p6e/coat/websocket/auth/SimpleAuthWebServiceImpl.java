package club.p6e.coat.websocket.auth;

import club.p6e.coat.common.utils.GeneratorUtil;
import club.p6e.coat.websocket.User;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;

/**
 * @author lidashuang
 * @version 1.0
 */
@Component
@ConditionalOnMissingBean(
        value = AuthWebService.class,
        ignored = SimpleAuthWebServiceImpl.class
)
@ConditionalOnClass(name = "org.springframework.web.servlet.package-info")
public class SimpleAuthWebServiceImpl implements AuthWebService {

    @Override
    public String award(HttpServletRequest request) {
        return GeneratorUtil.uuid();
    }

    @Override
    public User validate(String voucher) {
        return () -> voucher;
    }

}
