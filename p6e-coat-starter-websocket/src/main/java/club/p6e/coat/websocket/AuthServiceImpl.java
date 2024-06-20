package club.p6e.coat.websocket;

import club.p6e.coat.common.utils.AesUtil;
import club.p6e.coat.common.utils.JsonUtil;
import club.p6e.coat.common.utils.Md5Util;
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
                final String[] data = voucher.split("\\.");
                if (data.length == 2) {
                    final String base64 = data[0];
                    final String paramSignature = data[1];
                    final String currentSignature = AesUtil.decryption(
                            Md5Util.execute(base64), AesUtil.stringToKey(getSecretData()));
                    if (currentSignature.equals(paramSignature)) {
                        final int index = base64.lastIndexOf("@");
                        if (index > 0) {
                            final String timestamp = base64.substring(index);
                            if (Long.parseLong(timestamp) + 900 > (System.currentTimeMillis() / 1000)) {
                                return getUserData(base64.substring(0, index));
                            }
                        }
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
