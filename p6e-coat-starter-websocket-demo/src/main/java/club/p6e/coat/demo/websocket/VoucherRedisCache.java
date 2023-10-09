package club.p6e.coat.demo.websocket;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * @author lidashuang
 * @version 1.0
 */
@Component
public class VoucherRedisCache implements VoucherCache {

    /**
     * 缓存对象
     */
    private final StringRedisTemplate template;

    /**
     * 构造方法初始化
     *
     * @param template 缓存对象
     */
    public VoucherRedisCache(StringRedisTemplate template) {
        this.template = template;
    }

    @Override
    public void set(String voucher, String content) {
        template.opsForValue().set(VOUCHER_PREFIX + voucher, content, Duration.ofSeconds(EXPIRATION_TIME));
    }

    @Override
    public String get(String voucher) {
        return template.opsForValue().get(VOUCHER_PREFIX + voucher);
    }

    @Override
    public void del(String voucher) {
        template.delete(VOUCHER_PREFIX + voucher);
    }

}
