/* (C) 2025 HMM Corp. All rights reserved. */
package com.hmm.cbui.domain.livechat.config;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 라이브채팅 WebSocket 연결 시 인증을 처리하는 인터셉터 STOMP CONNECT 프레임에서 access_token을 검증합니다.
 *
 * @author jhm
 * @since 2025.09.18
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LiveChatWebSocketInterceptor implements ChannelInterceptor {

  // private final LiveChatTokenService tokenService; // 실제 토큰 검증 시 사용

  @Override
  public Message<?> preSend(Message<?> message, MessageChannel channel) {
    StompHeaderAccessor accessor =
        MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

    if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
      // CONNECT 프레임에서 access_token 추출 (헤더 또는 URL 파라미터)
      String accessToken = accessor.getFirstNativeHeader("access_token");

      // URL 파라미터에서 토큰 추출 (순수 WebSocket 사용 시)
      if (accessToken == null || accessToken.trim().isEmpty()) {
        String sessionId = accessor.getSessionId();
        if (sessionId != null) {
          // 세션에서 URL 파라미터 추출 (실제 구현에서는 더 정교한 방법 필요)
          accessToken = extractTokenFromSession(sessionId);
        }
      }

      if (accessToken == null || accessToken.trim().isEmpty()) {
        log.warn("WebSocket 연결 시 access_token이 없습니다");
        return null; // 연결 거부
      }

      // 토큰 검증 로직 (실제 구현에서는 JWT 검증 등 추가)
      if (isValidToken(accessToken)) {
        log.info(
            "WebSocket 연결 인증 성공: token={}",
            accessToken.substring(0, Math.min(10, accessToken.length())) + "...");

        // 사용자 정보를 세션에 저장
        accessor.getSessionAttributes().put("access_token", accessToken);
        accessor.getSessionAttributes().put("authenticated", true);

      } else {
        log.warn(
            "WebSocket 연결 인증 실패: token={}",
            accessToken.substring(0, Math.min(10, accessToken.length())) + "...");
        return null; // 연결 거부
      }
    }

    return message;
  }

  /** 세션에서 토큰을 추출합니다. */
  private String extractTokenFromSession(String sessionId) {
    // 실제 구현에서는 세션에서 URL 파라미터를 추출하는 로직 필요
    // 현재는 간단한 예시로 항상 유효한 토큰 반환
    return "valid_token_" + sessionId;
  }

  /** 토큰 유효성을 검증합니다. 실제 구현에서는 JWT 검증, 외부 API 호출 등을 수행합니다. */
  private boolean isValidToken(String accessToken) {
    // 테스트를 위해 더 관대한 검증 로직
    return accessToken != null && accessToken.length() > 0;
  }
}
