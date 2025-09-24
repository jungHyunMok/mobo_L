/* (C) 2025 HMM Corp. All rights reserved. */
package com.hmm.cbui.core.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import com.hmm.cbui.domain.livechat.config.LiveChatWebSocketInterceptor;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

  private final LiveChatWebSocketInterceptor webSocketInterceptor;

  @Override
  public void configureMessageBroker(MessageBrokerRegistry config) {
    // Redis를 이용한 메시지 브로커 설정
    config
        .enableStompBrokerRelay("/topic", "/queue")
        .setRelayHost("127.0.0.1")
        .setRelayPort(61613)
        .setClientLogin("guest")
        .setClientPasscode("guest")
        .setSystemLogin("guest")
        .setSystemPasscode("guest");

    // 클라이언트에서 서버로 메시지를 보낼 때 사용하는 prefix
    config.setApplicationDestinationPrefixes("/app");
  }

  @Override
  public void registerStompEndpoints(StompEndpointRegistry registry) {
    // WebSocket 연결 엔드포인트 설정 (SockJS 제거)
    registry
        .addEndpoint("/ws")
        .setAllowedOriginPatterns("*")
        .setHandshakeHandler(
            new org.springframework.web.socket.server.support.DefaultHandshakeHandler());
  }

  @Override
  public void configureClientInboundChannel(
      org.springframework.messaging.simp.config.ChannelRegistration registration) {
    // WebSocket 연결 시 인증 인터셉터 등록
    registration.interceptors(webSocketInterceptor);
  }
}
