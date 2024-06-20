package club.p6e.coat.websocket.controller;

import club.p6e.coat.common.context.ResultContext;
import club.p6e.coat.common.error.ParameterException;
import club.p6e.coat.common.utils.GeneratorUtil;
import club.p6e.coat.common.utils.NumberUtil;
import club.p6e.coat.websocket.WebSocketMain;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;

/**
 * WEB FLUX CONTROLLER
 *
 * @author lidashuang
 * @version 1.0
 */
@Component
@RestController
@ConditionalOnClass(name = "org.springframework.web.reactive.package-info")
public class WebFluxController extends Controller {

    /**
     * WebSocket Main 对象
     */
    private final WebSocketMain webSocketMain;

    /**
     * 构造方法初始化
     *
     * @param webSocketMain WebSocket Main 对象
     */
    public WebFluxController(WebSocketMain webSocketMain) {
        this.webSocketMain = webSocketMain;
    }

    @PostMapping("/push")
    public Mono<ResultContext> push(@RequestBody PushParam param) {
        return pushText(param);
    }

    @PostMapping("/push/text")
    public Mono<ResultContext> pushText(@RequestBody PushParam param) {
        if (param == null
                || param.getUsers() == null
                || param.getUsers().isEmpty()
                || param.getType() == null
                || param.getContent() == null) {
            throw new ParameterException(
                    this.getClass(),
                    "fun push(PushParam param).",
                    "request parameter exception, please check your network request."
            );
        }
        final String id = DATE_TIME_FORMATTER.format(LocalDateTime.now()) + GeneratorUtil.uuid();
        final String type = param.getType();
        final String content = param.getContent();
        final String name = param.getName() == null ? "DEFAULT" : param.getName();
        final List<String> users = param.getUsers();
        webSocketMain.push(user -> users.contains(user.id()), name, id, type, content);
        return Mono.just(ResultContext.build(id));
    }

    @PostMapping("/push/hex")
    public Mono<ResultContext> pushHex(@RequestBody PushParam param) {
        if (param == null
                || param.getUsers() == null
                || param.getUsers().isEmpty()
                || param.getContent() == null) {
            throw new ParameterException(
                    this.getClass(),
                    "fun push(PushParam param).",
                    "request parameter exception, please check your network request."
            );
        }
        final String id = DATE_TIME_FORMATTER.format(LocalDateTime.now()) + GeneratorUtil.uuid();
        final String content = param.getContent();
        final String name = param.getName() == null ? "DEFAULT" : param.getName();
        final List<String> users = param.getUsers();
        webSocketMain.push(user -> users.contains(user.id()), name, NumberUtil.hexToBytes(content));
        return Mono.just(ResultContext.build(id));
    }

}
