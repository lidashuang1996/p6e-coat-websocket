package club.p6e.coat.sse.controller;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 接口控制器
 *
 * @author lidashuang
 * @version 1.0
 */
public class Controller {

    @Data
    @Accessors(chain = true)
    public static class PushParam implements Serializable {
        private String name;
        private String type;
        private String content;
        private List<String> users;
    }

    /**
     * 时间格式化对象
     */
    public static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    /**
     * 请求参数名称
     */
    private static final String V_PARAM_NAME = "v";

    /**
     * 请求参数名称
     */
    private static final String VOUCHER_PARAM_NAME = "voucher";

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
                    final String kv = param.substring(pi, i == param.length() - 1 ? i + 1 : i);
                    final String[] kvs = kv.split("=");
                    if (kvs.length == 2) {
                        result.computeIfAbsent(kvs[0], k -> new ArrayList<>()).add(URLDecoder.decode(kvs[1], StandardCharsets.UTF_8));
                    }
                    pi = i + 1;
                }
            }
        }
        return result;
    }

    /**
     * 获取路径参数
     *
     * @param uri 请求的 URI 地址
     * @return 凭证参数
     */
    public static String getPath(String uri) {
        int index = uri.indexOf("?");
        if (index < 0) {
            index = uri.length();
        }
        return uri.substring(0, index);
    }

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
