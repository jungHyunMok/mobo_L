/* (C) 2025 HMM Corp. All rights reserved. */
package com.hmm.cbui.domain.livechat.service;

import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.hmm.cbui.domain.livechat.dto.LiveChatAuthDto;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

/**
 * 라이브채팅 토큰 발급 서비스 외부 API와 연동하여 토큰을 발급받고 관리합니다.
 *
 * @author jhm
 * @since 2025.09.18
 */
@Slf4j
@Service("liveChatTokenApiService")
@RequiredArgsConstructor
public class LiveChatTokenService {

  private final WebClient webClient;

  @Value("${livechat.external.token-url:https://dlchatga.hmm21.com/cstalk/rest/anonymous/token}")
  private String tokenUrl;

  @Value(
      "${livechat.external.websocket-url:wss://dlchatlm.hmm21.com/websocket/060/gpbdnubi/websocket}")
  private String websocketUrl;

  /**
   * 외부 API로부터 라이브채팅 토큰을 발급받습니다.
   *
   * @param assertionToken HMM에서 발급한 assertion 토큰
   * @param userId 사용자 ID
   * @return 발급받은 토큰 정보
   */
  public Mono<LiveChatAuthDto> getAccessToken(String assertionToken, String userId) {
    log.info("라이브채팅 토큰 발급 요청: userId={}", userId);

    return webClient
        .post()
        .uri(tokenUrl)
        .bodyValue(
            Map.of(
                "assertionToken", assertionToken,
                "userId", userId))
        .retrieve()
        .bodyToMono(Map.class)
        .map(this::mapToAuthDto)
        .doOnNext(auth -> log.info("토큰 발급 성공: userId={}, isValid={}", userId, auth.getIsValid()))
        .doOnError(error -> log.error("토큰 발급 실패: userId={}", userId, error));
  }

  /**
   * 외부 WebSocket URL을 생성합니다.
   *
   * @param accessToken 발급받은 액세스 토큰
   * @return WebSocket 연결 URL
   */
  public String buildWebSocketUrl(String accessToken) {
    return websocketUrl + "?access_token=" + accessToken;
  }

  /** 외부 API 응답을 LiveChatAuthDto로 변환합니다. */
  private LiveChatAuthDto mapToAuthDto(Map<String, Object> response) {
    return LiveChatAuthDto.builder()
        .isValid((Boolean) response.get("isValid"))
        .userNm((String) response.get("userNm"))
        .liveChatToken((String) response.get("liveChatToken"))
        .cbtEmail((String) response.get("cbtEmail"))
        .build();
  }
}
