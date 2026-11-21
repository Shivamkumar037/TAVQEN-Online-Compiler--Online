
package com.compiler.backend.compiler.config;

import com.compiler.backend.compiler.websocket.CompilerWebSocketHandler;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

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
    public void registerWebSocketHandlers(
            WebSocketHandlerRegistry registry
    ) {

        registry.addHandler(
                handler,
                props.getWebsocketPath()
        ).setAllowedOriginPatterns("*");

    }
}
