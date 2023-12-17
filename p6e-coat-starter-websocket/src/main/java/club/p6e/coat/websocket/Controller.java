package club.p6e.coat.websocket;

import club.p6e.coat.common.context.ResultContext;
import club.p6e.coat.common.error.AuthException;
import club.p6e.coat.common.error.ParameterException;
import club.p6e.coat.common.utils.GeneratorUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Data;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 接口控制器
 *
 * @author lidashuang
 * @version 1.0
 */
@Component
@RestController
public class Controller {

    @Data
    public static class PushParam implements Serializable {
        private String name;
        private String type;
        private String content;
        private List<String> users;
    }

    /**
     * 时间格式化对象
     */
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    /**
     * 认证服务对象
     */
    private final AuthService authService;

    /**
     * WebSocket Main 对象
     */
    private final WebSocketMain webSocketMain;

    /**
     * 构造方法初始化
     *
     * @param authService   认证服务对象
     * @param webSocketMain WebSocket Main 对象
     */
    public Controller(AuthService authService, WebSocketMain webSocketMain) {
        this.authService = authService;
        this.webSocketMain = webSocketMain;
    }

    @RequestMapping("/auth")
    public ResultContext auth(HttpServletRequest request) {
        final String voucher = authService.award(request);
        if (voucher == null) {
            throw new AuthException(
                    this.getClass(),
                    "fun push(PushParam param).",
                    "auth error, please check your network request."
            );
        }
        return ResultContext.build(voucher);
    }

    @PostMapping("/push")
    public ResultContext push(@RequestBody PushParam param) {
        if (param == null
                || param.getType() == null
                || param.getContent() == null
                || param.getUsers() == null
                || param.getUsers().isEmpty()) {
            throw new ParameterException(
                    this.getClass(),
                    "fun push(PushParam param).",
                    "request parameter exception, please check your network request."
            );
        }
        final String id = DATE_TIME_FORMATTER.format(LocalDateTime.now()) + GeneratorUtil.uuid();
        final String name = param.getName() == null ? "DEFAULT" : param.getName();
        final String type = param.getType();
        final String content = param.getContent();
        final List<String> users = param.getUsers();
        webSocketMain.push(user -> users.contains(user.id()), name, id, type, content);
        return ResultContext.build(id);
    }

}
