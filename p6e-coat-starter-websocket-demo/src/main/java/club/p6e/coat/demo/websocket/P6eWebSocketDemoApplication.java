package club.p6e.coat.demo.websocket;

import club.p6e.coat.websocket.EnableP6eWebSocket;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author lidashuang
 * @version 1.0.0
 */
@EnableP6eWebSocket
@SpringBootApplication
public class P6eWebSocketDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(P6eWebSocketDemoApplication.class, args);
    }

}
