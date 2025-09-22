/* (C) 2025 HMM Corp. All rights reserved. */
package com.hmm.cbui.domain.auth.service;

import java.util.Map;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

/**
 * 외부 토큰 검증 서비스 실제 외부 토큰 검증 API를 호출하여 토큰의 유효성을 검증합니다.
 *
 * @author jhm
 * @since 2025.01.20
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ExternalTokenValidationService {

  // 외부 토큰 검증 API URL (실제 연동 시 설정)
  @SuppressWarnings("unused")
  private static final String EXTERNAL_TOKEN_VALIDATION_URL =
      "https://external-auth-api.com/validate";

  /**
   * 외부 API를 통해 토큰 검증
   *
   * @param token 검증할 토큰
   * @return 토큰 검증 결과 (Map 형태로 사용자 정보 포함)
   */
  public Mono<Map<String, Object>> validateToken(String token) {
    log.info("외부 토큰 검증 요청: token={}", token.substring(0, Math.min(token.length(), 20)) + "...");

    // 실제 외부 API 호출 (현재는 모킹)
    // Map<String, String> headers = new HashMap<>();
    // headers.put("Authorization", "Bearer " + token);
    // headers.put("Content-Type", "application/json");
    //
    // return webClientService.post(EXTERNAL_TOKEN_VALIDATION_URL,
    //     Map.of("token", token), headers, Map.class);

    // 현재는 모킹된 응답 반환
    return Mono.just(mockTokenValidationResponse(token));
  }

  /** 토큰 검증 모킹 응답 실제 구현 시에는 외부 API 응답을 그대로 반환 */
  private Map<String, Object> mockTokenValidationResponse(String token) {
    // 간단한 토큰 검증 로직 (실제로는 외부 API에서 검증)
    boolean isValid = token != null && token.length() > 10;

    return Map.of(
        "isValid", isValid,
        "userId", isValid ? "user_" + token.hashCode() : null,
        "userName", isValid ? "홍길동" : null,
        "email", isValid ? "user@hmm21.com" : null,
        "expiresAt", isValid ? System.currentTimeMillis() + 3600000 : null);
  }
}
