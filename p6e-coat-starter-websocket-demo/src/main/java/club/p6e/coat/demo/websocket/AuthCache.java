package club.p6e.coat.demo.websocket;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.List;

/**
 * 认证缓存服务
 *
 * @author lidashuang
 * @version 1.0
 */
public interface AuthCache {

    /**
     * 令牌模型
     */
    @Data
    @Accessors(chain = true)
    class Token implements Serializable {

        /**
         * UID
         */
        private String uid;

        /**
         * ACCESS TOKEN
         */
        private String accessToken;

    }

    /**
     * 令牌模型
     */
    @Data
    @Accessors(chain = true)
    class User implements Serializable {
        private String id;
        private String name;
        private List<String> tags;
    }

    /**
     * 用户缓存前缀
     */
    String USER_PREFIX = "AUTH:USER:";

    /**
     * ACCESS TOKEN 缓存前缀
     */
    String ACCESS_TOKEN_PREFIX = "AUTH:ACCESS_TOKEN:";

    /**
     * 读取用户内容
     *
     * @param uid 用户
     * @return 读取用户内容
     */
    User getUser(String uid);

    /**
     * 读取 ACCESS TOKEN 令牌内容
     *
     * @param token 令牌
     * @return ACCESS TOKEN 令牌内容
     */
    Token getAccessToken(String token);

}
