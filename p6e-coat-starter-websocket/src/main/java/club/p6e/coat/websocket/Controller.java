package club.p6e.coat.websocket;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author lidashuang
 * @version 1.0
 */
@RestController
public class Controller {

    @RequestMapping("/auth")
    public String auth() {
        return "";
    }

    @RequestMapping("/push")
    public String push() {
        return "";
    }

}
