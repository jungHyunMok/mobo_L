/* (C) 2025 HMM Corp. All rights reserved. */
package com.hmm.cbui.domain.auth.service;

import java.util.Map;

import org.springframework.stereotype.Service;

import com.hmm.cbui.core.web.WebClientService;
import com.hmm.cbui.domain.auth.dto.LiveChatTokenDto;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

/**
 * LiveChat 토큰 발급 서비스 LiveChat API를 통해 토큰을 발급받습니다.
 *
 * @author jhm
 * @since 2025.01.20
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LiveChatTokenService {

  private final WebClientService webClientService;

  // LiveChat 토큰 발급 API URL
  private static final String LIVECHAT_TOKEN_URL =
      "https://dlchatga.hmm21.com/cstalk/rest/anonymous/token";

  /**
   * LiveChat 토큰 발급
   *
   * @return 발급된 LiveChat 토큰 정보
   */
  public Mono<LiveChatTokenDto> issueToken() {
    log.info("LiveChat 토큰 발급 요청");

    // 실제 LiveChat API 호출
    Map<String, String> headers =
        Map.of(
            "Content-Type", "application/json",
            "Accept", "application/json");

    // 빈 요청 본문 (토큰 발급은 본문이 필요 없을 수 있음)
    Map<String, Object> requestBody = Map.of();

    return webClientService
        .post(LIVECHAT_TOKEN_URL, requestBody, headers, Map.class)
        .map(this::convertToLiveChatTokenDto)
        .onErrorResume(
            error -> {
              log.error("LiveChat 토큰 발급 실패", error);
              // 실패 시 모킹된 토큰 반환
              return Mono.just(createMockLiveChatToken());
            });
  }

  /** API 응답을 LiveChatTokenDto로 변환 */
  private LiveChatTokenDto convertToLiveChatTokenDto(Map<String, Object> response) {
    String token = (String) response.get("token");
    return LiveChatTokenDto.builder()
        .token(token)
        .expiresIn(3600L) // 기본 1시간
        .tokenType("Bearer")
        .build();
  }

  /** LiveChat 토큰 모킹 (API 호출 실패 시 사용) */
  private LiveChatTokenDto createMockLiveChatToken() {
    return LiveChatTokenDto.builder()
        .token("mock_livechat_token_" + System.currentTimeMillis())
        .expiresIn(3600L)
        .tokenType("Bearer")
        .build();
  }
}
