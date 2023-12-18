package club.p6e.coat.websocket;

import java.io.Serializable;
import java.util.Map;

/**
 * 用户对象
 *
 * @author lidashuang
 * @version 1.0
 */
public interface User extends Serializable {

    /**
     * 用户编号
     *
     * @return 用户编号
     */
    public String id();

    /**
     * MAP
     */
    public Map<String, Object> toMap();

}
