package club.p6e.coat.demo.websocket;

/**
 * @author lidashuang
 * @version 1.0
 */
public interface VoucherCache {

    /**
     * 过期时间
     */
    long EXPIRATION_TIME = 300;

    /**
     * VOUCHER 缓存前缀
     */
    String VOUCHER_PREFIX = "WEBSOCKET:VOUCHER:";

    /**
     * 写入凭证内容
     *
     * @param voucher 凭证
     * @param content 内容
     */
    void set(String voucher, String content);

    /**
     * 读取凭证
     *
     * @param voucher 凭证
     * @return 凭证内容
     */
    String get(String voucher);

    /**
     * 删除凭证
     *
     * @param voucher 凭证
     */
    void del(String voucher);

}
