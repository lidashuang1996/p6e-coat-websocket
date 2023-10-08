package club.p6e.coat.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.InputStream;
import java.util.Map;

/**
 * JSON 序列化和反序列化帮助类
 *
 * @author lidashuang
 * @version 1.0
 */
public class JsonUtil {

    /**
     * OBJECT_MAPPER 对象
     */
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    /**
     * 序列化对象
     *
     * @param o 对象
     * @return 序列化内容
     */
    @SuppressWarnings("ALL")
    public static String toJson(Object o) {
        try {
            return OBJECT_MAPPER.writeValueAsString(o);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 反序列化 JSON 到对象
     *
     * @param json   json 内容
     * @param tClass 对象类型
     * @param <T>    类型
     * @return 对象
     */
    @SuppressWarnings("ALL")
    public static <T> T fromJson(String json, Class<T> tClass) {
        try {
            return OBJECT_MAPPER.readValue(json, tClass);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 反序列化 JSON 到对象
     *
     * @param inputStream json 内容流
     * @param tClass      对象类型
     * @param <T>         类型
     * @return 对象
     */
    @SuppressWarnings("ALL")
    public static <T> T fromJson(InputStream inputStream, Class<T> tClass) {
        try {
            return OBJECT_MAPPER.readValue(inputStream, tClass);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 反序列化 JSON 到对象
     *
     * @param json   json 内容
     * @param kClass key 对象类型
     * @param vClass value 对象类型
     * @param <K>    key 对象类型
     * @param <V>    value 对象类型
     * @return 对象
     */
    @SuppressWarnings("ALL")
    public static <K, V> Map<K, V> fromJsonToMap(String json, Class<K> kClass, Class<V> vClass) {
        try {
            return OBJECT_MAPPER.readValue(json, OBJECT_MAPPER.getTypeFactory().constructParametricType(Map.class, kClass, vClass));
        } catch (Exception e) {
            return null;
        }
    }

}
