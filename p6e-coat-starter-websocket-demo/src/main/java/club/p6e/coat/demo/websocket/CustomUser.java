package club.p6e.coat.demo.websocket;

import club.p6e.coat.websocket.User;

import java.util.List;
import java.util.Map;

/**
 * @author lidashuang
 * @version 1.0
 */
public class CustomUser implements User {

    private final String id;
    private final List<String> tags;

    public CustomUser(String id, List<String> tags) {
        this.id = id;
        this.tags = tags;
    }

    @Override
    public String id() {
        return id;
    }

    @Override
    public Map<String, Object> toMap() {
        return null;
    }

    public List<String> tags() {
        return tags;
    }

}
