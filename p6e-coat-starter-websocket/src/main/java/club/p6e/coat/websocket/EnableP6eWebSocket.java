package club.p6e.coat.websocket;

import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * 注解启动 WebSocket 服务
 *
 * @author lidashuang
 * @version 1.0
 */
@Documented
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Import({
        AutoConfigureImportSelector.class
})
public @interface EnableP6eWebSocket {

    /**
     * 配置注解
     */
    @Documented
    @Target({ElementType.METHOD})
    @Retention(RetentionPolicy.RUNTIME)
    public @interface Config {
        String name() default "DEFAULT";

        int port() default 9600;
    }

    /**
     * 设置配置注解
     *
     * @return 配置注解对象
     */
    Config[] value() default {};

    /**
     * 设置线程池大小
     *
     * @return 线程池大小
     */
    int threadPoolLength() default 10;

}
