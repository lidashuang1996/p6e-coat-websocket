package club.p6e.coat.sse.controller;

import club.p6e.coat.common.context.ResultContext;
import club.p6e.coat.common.error.ParameterException;
import club.p6e.coat.common.utils.GeneratorUtil;
import club.p6e.coat.sse.Server;
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
     * 服务对象
     */
    private final Server server;

    /**
     * 构造方法初始化
     *
     * @param server 服务对象
     */
    public WebFluxController(Server server) {
        this.server = server;
    }

    @PostMapping("/push")
    public Mono<ResultContext> push(@RequestBody PushParam param) {
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
        final List<String> users = param.getUsers();
        final String name = param.getName() == null ? "DEFAULT" : param.getName();
        server.push(user -> users.contains(user.id()), name, id, type, content);
        return Mono.just(ResultContext.build(id));
    }

}
