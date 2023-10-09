package club.p6e.coat.demo.websocket;

import club.p6e.coat.common.context.ResultContext;
import club.p6e.coat.common.error.ParameterException;
import club.p6e.coat.common.utils.GeneratorUtil;
import club.p6e.coat.websocket.WebSocketMain;
import lombok.Data;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * @author lidashuang
 * @version 1.0
 */
@RestController
public class CustomController {

    @Data
    public static class PushParam {
        private String name;
        private String type;
        private String content;
        private List<String> tags;
    }

    /**
     * 时间格式化对象
     */
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    /**
     * WebSocket Main 对象
     */
    private final WebSocketMain webSocketMain;

    /**
     * 构造方法初始化
     *
     * @param webSocketMain WebSocket Main 对象
     */
    public CustomController(WebSocketMain webSocketMain) {
        this.webSocketMain = webSocketMain;
    }

    @PostMapping("/push/tags")
    public ResultContext pushTags(@RequestBody PushParam param) {
        // 自定义请求内容来实现推送
        System.out.println(param);
        if (param == null
                || param.getType() == null
                || param.getContent() == null
                || param.getTags() == null
                || param.getTags().isEmpty()) {
            throw new ParameterException(this.getClass(), "fun push(PushParam param).");
        }
        System.out.println(param);
        final String id = DATE_TIME_FORMATTER.format(LocalDateTime.now()) + GeneratorUtil.uuid();
        final String name = param.getName() == null ? "DEFAULT" : param.getName();
        final String type = param.getType();
        final String content = param.getContent();
        final List<String> tags = param.getTags();
        // 通过自定义的 USER 对象来扩展复杂的业务场景下的推送需求
        webSocketMain.push(u -> {
            // CustomUser 自定义的 USER 对象
            if (u instanceof final CustomUser user) {
                for (final String tag : tags) {
                    // 通过自定义的 TAGS 来实现推送
                    if (user.tags().contains(tag)) {
                        return true;
                    }
                }
            }
            return false;
        }, name, id, type, content);
        return ResultContext.build(id);
    }

}
