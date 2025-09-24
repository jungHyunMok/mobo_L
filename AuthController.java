/* (C) 2025 HMM Corp. All rights reserved. */
package com.hmm.cbui.domain.auth.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.hmm.cbui.domain.auth.dto.AuthRequestDto;
import com.hmm.cbui.domain.auth.dto.AuthResponseDto;
import com.hmm.cbui.domain.auth.dto.CustomerIdDto;
import com.hmm.cbui.domain.auth.dto.LiveChatTokenDto;
import com.hmm.cbui.domain.auth.service.ExternalTokenValidationService;
import com.hmm.cbui.domain.auth.service.LiveChatCustomerService;
import com.hmm.cbui.domain.auth.service.LiveChatTokenService;
import com.hmm.cbui.domain.livechat.dto.ApiResponseDto;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

/**
 * 인증 관련 REST API 컨트롤러 토큰 검증부터 LiveChat 토큰 발급, Customer ID 생성, WebSocket 연결까지 통합 처리
 *
 * @author jhm
 * @since 2025.01.20
 */
@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

  private final ExternalTokenValidationService tokenValidationService;
  private final LiveChatTokenService liveChatTokenService;
  private final LiveChatCustomerService liveChatCustomerService;

  // WebSocket 기본 URL
  private static final String WEBSOCKET_BASE_URL = "wss://echo.websocket.org";

  // "wss://dlchatlm.hmm21.com/websocket/060/gpbdnubi/websocket";

  /**
   * 통합 인증 처리 API 1. 외부 토큰 검증 2. LiveChat 토큰 발급 3. Customer ID 생성 4. WebSocket 연결 URL 생성
   *
   * @param request 인증 요청 정보
   * @return 인증 결과 및 WebSocket 연결 정보
   */
  @PostMapping("/authenticate")
  public Mono<ResponseEntity<ApiResponseDto<AuthResponseDto>>> authenticate(
      @Valid @RequestBody AuthRequestDto request) {

    log.info(
        "통합 인증 요청: token={}",
        request.getToken().substring(0, Math.min(request.getToken().length(), 20)) + "...");

    return tokenValidationService
        .validateToken(request.getToken())
        .flatMap(
            validationResult -> {
              Boolean isValid = (Boolean) validationResult.get("isValid");

              if (!isValid) {
                log.warn("토큰 검증 실패");
                return Mono.just(createErrorResponse("토큰 검증에 실패했습니다"));
              }

              String userId = (String) validationResult.get("userId");
              String userName = (String) validationResult.get("userName");
              String email = (String) validationResult.get("email");

              log.info("토큰 검증 성공: userId={}, userName={}", userId, userName);

              // LiveChat 토큰 발급과 Customer ID 생성을 병렬로 처리
              return Mono.zip(
                      liveChatTokenService.issueToken(),
                      liveChatCustomerService.createCustomerId(
                          email != null ? email : "user@hmm21.com"))
                  .map(
                      tuple -> {
                        LiveChatTokenDto liveChatToken = tuple.getT1();
                        CustomerIdDto customerId = tuple.getT2();

                        // WebSocket 연결 URL 생성
                        String websocketUrl =
                            WEBSOCKET_BASE_URL + "?access_token=" + liveChatToken.getToken();

                        AuthResponseDto response =
                            AuthResponseDto.builder()
                                .isValid(true)
                                .userId(userId)
                                .userName(userName)
                                .liveChatToken(liveChatToken)
                                .customerId(customerId)
                                .websocketUrl(websocketUrl)
                                .authSuccess(true)
                                .build();

                        log.info(
                            "인증 완료: userId={}, liveChatToken={}, customerId={}",
                            userId,
                            liveChatToken.getToken(),
                            customerId.getCustomerId());

                        return ResponseEntity.ok(
                            ApiResponseDto.success("인증이 성공적으로 완료되었습니다", response));
                      });
            })
        .onErrorResume(
            error -> {
              log.error("인증 처리 중 오류 발생", error);
              return Mono.just(createErrorResponse("인증 처리 중 오류가 발생했습니다: " + error.getMessage()));
            });
  }

  /**
   * 토큰 검증만 수행하는 API (선택적)
   *
   * @param request 인증 요청 정보
   * @return 토큰 검증 결과
   */
  @PostMapping("/validate")
  public Mono<ResponseEntity<ApiResponseDto<Map<String, Object>>>> validateToken(
      @Valid @RequestBody AuthRequestDto request) {

    log.info(
        "토큰 검증 요청: token={}",
        request.getToken().substring(0, Math.min(request.getToken().length(), 20)) + "...");

    return tokenValidationService
        .validateToken(request.getToken())
        .map(
            validationResult ->
                ResponseEntity.ok(ApiResponseDto.success("토큰 검증이 완료되었습니다", validationResult)))
        .onErrorResume(
            error -> {
              log.error("토큰 검증 중 오류 발생", error);
              return Mono.just(
                  ResponseEntity.badRequest()
                      .body(ApiResponseDto.error("토큰 검증 중 오류가 발생했습니다: " + error.getMessage())));
            });
  }

  /**
   * LiveChat 토큰 발급 API (선택적)
   *
   * @return 발급된 LiveChat 토큰
   */
  @PostMapping("/livechat/token")
  public Mono<ResponseEntity<ApiResponseDto<LiveChatTokenDto>>> issueLiveChatToken() {
    log.info("LiveChat 토큰 발급 요청");

    return liveChatTokenService
        .issueToken()
        .map(
            token -> ResponseEntity.ok(ApiResponseDto.success("LiveChat 토큰이 성공적으로 발급되었습니다", token)))
        .onErrorResume(
            error -> {
              log.error("LiveChat 토큰 발급 중 오류 발생", error);
              return Mono.just(
                  ResponseEntity.badRequest()
                      .body(
                          ApiResponseDto.error(
                              "LiveChat 토큰 발급 중 오류가 발생했습니다: " + error.getMessage())));
            });
  }

  /**
   * Customer ID 생성 API (선택적)
   *
   * @param loginId 로그인 ID
   * @return 생성된 Customer ID
   */
  @PostMapping("/livechat/customer")
  public Mono<ResponseEntity<ApiResponseDto<CustomerIdDto>>> createCustomerId(
      @RequestParam String loginId) {
    log.info("Customer ID 생성 요청: loginId={}", loginId);

    return liveChatCustomerService
        .createCustomerId(loginId)
        .map(
            customerId ->
                ResponseEntity.ok(ApiResponseDto.success("Customer ID가 성공적으로 생성되었습니다", customerId)))
        .onErrorResume(
            error -> {
              log.error("Customer ID 생성 중 오류 발생", error);
              return Mono.just(
                  ResponseEntity.badRequest()
                      .body(
                          ApiResponseDto.error(
                              "Customer ID 생성 중 오류가 발생했습니다: " + error.getMessage())));
            });
  }

  /** 에러 응답 생성 헬퍼 메서드 */
  private ResponseEntity<ApiResponseDto<AuthResponseDto>> createErrorResponse(String message) {
    return ResponseEntity.badRequest().body(ApiResponseDto.error(message));
  }
}
