package club.p6e.coat.sse;

import java.io.Serializable;

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
    String id();

}
