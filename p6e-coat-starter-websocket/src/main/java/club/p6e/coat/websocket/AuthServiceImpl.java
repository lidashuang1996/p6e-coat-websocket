package club.p6e.coat.websocket;

import club.p6e.coat.common.utils.AesUtil;
import club.p6e.coat.common.utils.JsonUtil;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 认证接口
 *
 * @author lidashuang
 * @version 1.0
 */
@Component
public class AuthServiceImpl implements AuthService {

    @Override
    public User validate(String voucher) {
        try {
            if (voucher != null && !voucher.isEmpty()) {
                final String content = AesUtil.decryption(voucher, AesUtil.stringToKey(getSecretData()));
                final int index = content.lastIndexOf("@");
                if (index > 0) {
                    final String timestamp = content.substring((index + 1));
                    if ((Long.parseLong(timestamp) + 15) > (System.currentTimeMillis() / 1000L)) {
                        return getUserData(content.substring(0, index));
                    }
                }
            }
        } catch (Exception ignore) {
        }
        return null;
    }

    public String getSecretData() {
        throw new RuntimeException("[ " + this.getClass() + " ::: getSecretData() ] >>> Please configure the key content.");
    }

    public User getUserData(String content) {
        if (content != null) {
            final Map<String, Object> cm = JsonUtil.fromJsonToMap(content, String.class, Object.class);
            if (cm != null && cm.get("id") != null) {
                final String id = String.valueOf(cm.get("id"));
                return () -> id;
            }
        }
        return null;
    }

}
