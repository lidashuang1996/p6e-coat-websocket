package club.p6e.coat.websocket;

import io.netty.channel.ChannelHandlerContext;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * @author lidashuang
 * @version 1.0
 */
@Component
public class DefaultAuth implements Auth<User> {

    @Override
    public String award(HttpServletRequest request) {
        return GeneratorUtil.uuid();
    }

    @Override
    public Session<User> validate(String uri, ChannelHandlerContext context) {
        final Map<String, String> params = getParams(uri);
        if (params == null || params.isEmpty()
                || (params.get("v") == null && params.get("voucher") == null)) {
            return null;
        }
        final String voucher = params.get("v") == null ? params.get("voucher") : params.get("v");
        return new Session<>(context, (User) () -> voucher);
    }

    protected Map<String, String> getParams(String uri) {
        final int index = uri.indexOf("?");
        final Map<String, String> result = new HashMap<>();
        if (index > 0) {
            int pi = 0;
            final String param = uri.substring(index + 1);
            for (int i = 0; i < param.length(); i++) {
                final char ch = param.charAt(i);
                if (ch == '&' || ch == '?') {
                    final String kv = param.substring(pi, i);
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

}
