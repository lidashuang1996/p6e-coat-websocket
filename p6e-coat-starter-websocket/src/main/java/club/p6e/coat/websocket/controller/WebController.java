package club.p6e.coat.websocket.controller;

import club.p6e.coat.common.context.ResultContext;
import club.p6e.coat.common.error.AuthException;
import club.p6e.coat.common.error.ParameterException;
import club.p6e.coat.common.utils.GeneratorUtil;
import club.p6e.coat.common.utils.NumberUtil;
import club.p6e.coat.websocket.WebSocketMain;
import club.p6e.coat.websocket.auth.AuthWebService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author lidashuang
 * @version 1.0
 */
@Component
@RestController
@ConditionalOnClass(name = "org.springframework.web.servlet.package-info")
public class WebController extends Controller {

    /**
     * 认证服务对象
     */
    private final AuthWebService authService;

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
    public WebController(AuthWebService authService, WebSocketMain webSocketMain) {
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
        return pushText(param);
    }

    @PostMapping("/push/text")
    public ResultContext pushText(@RequestBody PushParam param) {
        if (param == null
                || param.getType() == null
                || param.getContent() == null
                || param.getUsers() == null
                || param.getUsers().isEmpty()) {
            throw new ParameterException(
                    this.getClass(),
                    "fun pushText(PushParam param).",
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

    @PostMapping("/push/hex")
    public ResultContext pushHex(@RequestBody PushParam param) {
        if (param == null
                || param.getContent() == null
                || param.getUsers() == null
                || param.getUsers().isEmpty()) {
            throw new ParameterException(
                    this.getClass(),
                    "fun pushHex(PushParam param).",
                    "request parameter exception, please check your network request."
            );
        }
        final String id = DATE_TIME_FORMATTER.format(LocalDateTime.now()) + GeneratorUtil.uuid();
        final String name = param.getName() == null ? "DEFAULT" : param.getName();
        final String content = param.getContent();
        final List<String> users = param.getUsers();
        webSocketMain.push(user -> users.contains(user.id()), name, NumberUtil.hexToBytes(content));
        return ResultContext.build(id);
    }

}
