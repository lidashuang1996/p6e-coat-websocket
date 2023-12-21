package club.p6e.coat.websocket;

import club.p6e.coat.common.utils.GeneratorUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 认证（默认）
 *
 * @author lidashuang
 * @version 1.0
 */
@Component
@ConditionalOnMissingBean(
        value = AuthService.class,
        ignored = DefaultAuthServiceImpl.class
)
public class DefaultAuthServiceImpl implements AuthService {

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
    public static Map<String, List<String>> getParams(String uri) {
        final int index = uri.indexOf("?");
        final Map<String, List<String>> result = new HashMap<>(16);
        if (index > 0) {
            int pi = 0;
            final String param = uri.substring(index + 1);
            for (int i = 0; i < param.length(); i++) {
                final char ch = param.charAt(i);
                if (ch == '&' || ch == '?' || i == param.length() - 1) {
                    final String kv = param.substring(pi, i + 1);
                    final String[] kvs = kv.split("=");
                    if (kvs.length == 2) {
                        result.computeIfAbsent(kvs[0], k -> new ArrayList<>()).add(kvs[1]);
                    }
                    pi = i + 1;
                }
            }
        }
        return result;
    }

    /**
     * 请求参数名称
     */
    private static final String V_PARAM_NAME = "v";

    /**
     * 请求参数名称
     */
    private static final String VOUCHER_PARAM_NAME = "voucher";

    /**
     * 获取凭证参数
     *
     * @param uri 请求的 URI 地址
     * @return 凭证参数
     */
    public static String getVoucher(String uri) {
        String voucher;
        final Map<String, List<String>> params = getParams(uri);
        if (params.get(V_PARAM_NAME) == null || params.get(V_PARAM_NAME).isEmpty()) {
            if (params.get(VOUCHER_PARAM_NAME) == null || params.get(VOUCHER_PARAM_NAME).isEmpty()) {
                return null;
            } else {
                voucher = params.get(VOUCHER_PARAM_NAME).get(0);
            }
        } else {
            voucher = params.get(V_PARAM_NAME).get(0);
        }
        return voucher;
    }

}
