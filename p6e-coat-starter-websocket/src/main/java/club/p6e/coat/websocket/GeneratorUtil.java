package club.p6e.coat.websocket;

import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 生成序号的帮助类
 *
 * @author lidashuang
 * @version 1.0
 */
public final class GeneratorUtil {

    /**
     * 基础的字符模型
     */
    private static final String[] BASE_DATA = new String[]{
            "0", "1", "2", "3", "4", "5", "6", "7", "8", "9",
            "a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m",
            "n", "o", "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z",
            "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M",
            "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z",
    };

    /**
     * 生成 UUID 数据
     *
     * @return UUID
     */
    @SuppressWarnings("ALL")
    public static String uuid() {
        return UUID.randomUUID().toString().replaceAll("-", "");
    }

    /**
     * 生成 6 位随机数
     *
     * @return 6 位随机数
     */
    @SuppressWarnings("ALL")
    public static String random() {
        return random(6, false, false);
    }

    /**
     * 生产指定长度的随机数
     *
     * @param len 长度
     * @return 长度的随机数
     */
    @SuppressWarnings("ALL")
    public static String random(int len, boolean isLetter, boolean isCase) {
        final StringBuilder sb = new StringBuilder();
        final int base = isLetter ? (isCase ? 62 : 36) : 10;
        for (int i = 0; i < len; i++) {
            sb.append(BASE_DATA[ThreadLocalRandom.current().nextInt(base)]);
        }
        return sb.toString();
    }

}
