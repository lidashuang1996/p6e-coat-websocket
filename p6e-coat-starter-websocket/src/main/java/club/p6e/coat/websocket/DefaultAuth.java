package club.p6e.coat.websocket;

import club.p6e.coat.common.utils.GeneratorUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * 认证（默认）
 *
 * @author lidashuang
 * @version 1.0
 */
@Component
@ConditionalOnMissingBean(
        value = Auth.class,
        ignored = DefaultAuth.class
)
public class DefaultAuth implements Auth {

    @Override
    public String award(HttpServletRequest request) {
        return GeneratorUtil.uuid();
    }

    @Override
    public User validate(String uri) {
        final String voucher = getVoucher(uri);
        return () -> voucher;
    }

    /**
     * 获取 URL 参数
     *
     * @param uri 请求的 URL
     * @return 参数
     */
    public static Map<String, String> getParams(String uri) {
        final int index = uri.indexOf("?");
        final Map<String, String> result = new HashMap<>();
        if (index > 0) {
            int pi = 0;
            final String param = uri.substring(index + 1);
            for (int i = 0; i < param.length(); i++) {
                final char ch = param.charAt(i);
                if (ch == '&' || ch == '?' || i == param.length() - 1) {
                    final String kv = param.substring(pi, i + 1);
                    final String[] kvs = kv.split("=");
                    if (kvs.length == 2) {
                        result.put(kvs[0], kvs[1]);
                    }
                    pi = i + 1;
                }
            }
        }
        return result;
    }

    public static String getVoucher(String uri) {
        final Map<String, String> params = getParams(uri);
        if (params.isEmpty() || params.get("v") == null && params.get("voucher") == null) {
            return null;
        }
        return params.get("v") == null ? params.get("voucher") : params.get("v");
    }

}
