/* (C) 2025 HMM Corp. All rights reserved. */
package com.hmm.cbui.domain.livechat.controller;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hmm.cbui.domain.livechat.dto.ApiResponseDto;
import com.hmm.cbui.domain.livechat.dto.LiveChatAuthDto;
import com.hmm.cbui.domain.livechat.service.LiveChatTokenService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

/**
 * 라이브채팅 인증 관련 REST API 컨트롤러 외부 토큰 발급 API와 연동하여 클라이언트에 토큰을 제공합니다.
 *
 * @author jhm
 * @since 2025.09.18
 */
@Slf4j
@RestController
@RequestMapping("/api/livechat/auth")
@RequiredArgsConstructor
public class LiveChatAuthController {

  @Qualifier("liveChatTokenApiService")
  private final LiveChatTokenService tokenService;

  /** 라이브채팅 토큰 발급 POST /api/livechat/auth/token */
  @PostMapping("/token")
  public Mono<ResponseEntity<ApiResponseDto<LiveChatAuthDto>>> getToken(
      @Valid @RequestBody LiveChatAuthDto request) {

    log.info("라이브채팅 토큰 발급 요청: userId={}", request.getUserId());

    return tokenService
        .getAccessToken(request.getAssertionToken(), request.getUserId())
        .map(auth -> ResponseEntity.ok(ApiResponseDto.success("토큰이 성공적으로 발급되었습니다", auth)))
        .doOnNext(response -> log.info("토큰 발급 성공: userId={}", request.getUserId()))
        .onErrorResume(
            error -> {
              log.error("토큰 발급 실패: userId={}", request.getUserId(), error);
              return Mono.just(
                  ResponseEntity.badRequest()
                      .body(
                          ApiResponseDto.<LiveChatAuthDto>error(
                              "토큰 발급에 실패했습니다: " + error.getMessage())));
            });
  }
}
