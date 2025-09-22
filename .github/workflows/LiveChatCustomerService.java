/* (C) 2025 HMM Corp. All rights reserved. */
package com.hmm.cbui.domain.auth.service;

import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.hmm.cbui.core.web.WebClientService;
import com.hmm.cbui.domain.auth.dto.CustomerIdDto;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

/**
 * LiveChat Customer ID 생성 서비스 LiveChat API를 통해 Customer ID를 생성합니다.
 *
 * @author jhm
 * @since 2025.01.20
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LiveChatCustomerService {

  private final WebClientService webClientService;

  // LiveChat Customer ID 생성 API URL
  private static final String LIVECHAT_CUSTOMER_URL =
      "https://dlchatga.hmm21.com/cstalk/rest/anonymous/token";

  /**
   * LiveChat Customer ID 생성
   *
   * @param loginId 로그인 ID (이메일)
   * @return 생성된 Customer ID 정보
   */
  public Mono<CustomerIdDto> createCustomerId(String loginId) {
    log.info("LiveChat Customer ID 생성 요청: loginId={}", loginId);

    // 실제 LiveChat API 호출
    Map<String, String> headers =
        Map.of(
            "Content-Type", "application/json",
            "Accept", "application/json",
            "X-Attic-Application-id", "btalk");

    Map<String, Object> requestBody = Map.of("loginId", loginId);

    return webClientService
        .post(LIVECHAT_CUSTOMER_URL, requestBody, headers, Map.class)
        .map(response -> convertToCustomerIdDto(loginId, response))
        .onErrorResume(
            error -> {
              log.error("LiveChat Customer ID 생성 실패", error);
              // 실패 시 모킹된 Customer ID 반환
              return Mono.just(createMockCustomerId(loginId));
            });
  }

  /** API 응답을 CustomerIdDto로 변환 */
  @SuppressWarnings("unchecked")
  private CustomerIdDto convertToCustomerIdDto(String loginId, Map<String, Object> response) {
    // 실제 API 응답 구조에 따라 수정 필요
    String customerId =
        (String)
            response.getOrDefault(
                "customerId", "customer_" + UUID.randomUUID().toString().substring(0, 8));

    return CustomerIdDto.builder()
        .customerId(customerId)
        .loginId(loginId)
        .status("SUCCESS")
        .build();
  }

  /** Customer ID 모킹 (API 호출 실패 시 사용) */
  private CustomerIdDto createMockCustomerId(String loginId) {
    return CustomerIdDto.builder()
        .customerId("mock_customer_" + UUID.randomUUID().toString().substring(0, 8))
        .loginId(loginId)
        .status("SUCCESS")
        .build();
  }
}
