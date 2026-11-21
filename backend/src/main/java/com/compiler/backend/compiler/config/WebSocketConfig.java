
package com.compiler.backend.compiler.config;

import com.compiler.backend.compiler.websocket.CompilerWebSocketHandler;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

import java.util.Arrays;
import java.util.stream.Stream;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final CompilerProperties props;
    private final CompilerWebSocketHandler handler;

    public WebSocketConfig(
            CompilerProperties props,
            CompilerWebSocketHandler handler
    ) {
        this.props = props;
        this.handler = handler;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {

        String[] configuredOrigins = props.getAllowedOrigins()
                .split(",");

        String[] frontendOrigins = {
                "http://localhost:5500",
                "http://127.0.0.1:5500",
                "http://localhost:5501",
                "http://127.0.0.1:5501",
                "http://localhost:5173",
                "http://127.0.0.1:5173"
        };

        String[] allowedOrigins = Stream.concat(
                        Arrays.stream(configuredOrigins),
                        Arrays.stream(frontendOrigins)
                )
                .map(String::trim)
                .filter(origin -> !origin.isEmpty())
                .distinct()
                .toArray(String[]::new);

        registry.addHandler(handler, props.getWebsocketPath())
                .setAllowedOrigins(allowedOrigins);
    }
}
