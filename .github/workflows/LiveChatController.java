/* (C) 2025 HMM Corp. All rights reserved. */
package com.hmm.cbui.domain.livechat.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.hmm.cbui.domain.livechat.dto.*;
import com.hmm.cbui.domain.livechat.service.ExternalLiveChatApiService;
import com.hmm.cbui.domain.livechat.service.LiveChatService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

/** 라이브챗 REST API 컨트롤러 클라이언트와 외부 API 간의 중계 역할 */
@Slf4j
@RestController
@RequestMapping("/api/livechat")
@RequiredArgsConstructor
public class LiveChatController {

  private final ExternalLiveChatApiService externalApiService;
  private final LiveChatService liveChatService;

  /** 메시지 전송 POST /api/livechat/messages */
  @PostMapping("/messages")
  public Mono<ResponseEntity<ApiResponseDto<LiveChatResponseDto>>> sendMessage(
      @Valid @RequestBody LiveChatRequestDto request) {

    log.info(
        "메시지 전송 요청: roomId={}, sender={}, content={}",
        request.getRoomId(),
        request.getSender(),
        request.getContent());

    return externalApiService
        .sendMessage(request)
        .doOnNext(
            response -> {
              // 외부 API 응답을 받은 후, 웹소켓으로 실시간 전송
              LiveChatMessageDto message =
                  LiveChatMessageDto.builder()
                      .roomId(response.getRoomId())
                      .sender(response.getSender())
                      .content(response.getContent())
                      .timestamp(response.getTimestamp().toString())
                      .build();
              liveChatService.publish(response.getRoomId(), message);
            })
        .map(response -> ResponseEntity.ok(ApiResponseDto.success("메시지가 성공적으로 전송되었습니다", response)))
        .onErrorResume(
            error -> {
              log.error("메시지 전송 실패", error);
              return Mono.just(
                  ResponseEntity.badRequest()
                      .body(ApiResponseDto.error("메시지 전송에 실패했습니다: " + error.getMessage())));
            });
  }

  /** 메시지 목록 조회 GET /api/livechat/rooms/{roomId}/messages */
  @GetMapping("/rooms/{roomId}/messages")
  public Mono<ResponseEntity<ApiResponseDto<List<LiveChatResponseDto>>>> getMessages(
      @PathVariable String roomId,
      @RequestParam(defaultValue = "50") int limit,
      @RequestParam(defaultValue = "0") int offset) {

    log.info("메시지 목록 조회 요청: roomId={}, limit={}, offset={}", roomId, limit, offset);

    return externalApiService
        .getMessages(roomId, limit, offset)
        .map(
            messages -> ResponseEntity.ok(ApiResponseDto.success("메시지 목록을 성공적으로 조회했습니다", messages)))
        .onErrorResume(
            error -> {
              log.error("메시지 목록 조회 실패", error);
              return Mono.just(
                  ResponseEntity.badRequest()
                      .body(ApiResponseDto.error("메시지 목록 조회에 실패했습니다: " + error.getMessage())));
            });
  }

  /** 채팅방 정보 조회 GET /api/livechat/rooms/{roomId} */
  @GetMapping("/rooms/{roomId}")
  public Mono<ResponseEntity<ApiResponseDto<LiveChatRoomDto>>> getRoomInfo(
      @PathVariable String roomId) {
    log.info("채팅방 정보 조회 요청: roomId={}", roomId);

    return externalApiService
        .getRoomInfo(roomId)
        .map(room -> ResponseEntity.ok(ApiResponseDto.success("채팅방 정보를 성공적으로 조회했습니다", room)))
        .onErrorResume(
            error -> {
              log.error("채팅방 정보 조회 실패", error);
              return Mono.just(
                  ResponseEntity.badRequest()
                      .body(ApiResponseDto.error("채팅방 정보 조회에 실패했습니다: " + error.getMessage())));
            });
  }

  /** 채팅방 생성 POST /api/livechat/rooms */
  @PostMapping("/rooms")
  public Mono<ResponseEntity<ApiResponseDto<LiveChatRoomDto>>> createRoom(
      @Valid @RequestBody LiveChatRoomDto roomRequest) {

    log.info(
        "채팅방 생성 요청: roomName={}, createdBy={}",
        roomRequest.getRoomName(),
        roomRequest.getCreatedBy());

    return externalApiService
        .createRoom(roomRequest)
        .map(room -> ResponseEntity.ok(ApiResponseDto.success("채팅방이 성공적으로 생성되었습니다", room)))
        .onErrorResume(
            error -> {
              log.error("채팅방 생성 실패", error);
              return Mono.just(
                  ResponseEntity.badRequest()
                      .body(ApiResponseDto.error("채팅방 생성에 실패했습니다: " + error.getMessage())));
            });
  }

  /** 사용자 정보 조회 GET /api/livechat/users/{userId} */
  @GetMapping("/users/{userId}")
  public Mono<ResponseEntity<ApiResponseDto<LiveChatUserDto>>> getUserInfo(
      @PathVariable String userId) {
    log.info("사용자 정보 조회 요청: userId={}", userId);

    return externalApiService
        .getUserInfo(userId)
        .map(user -> ResponseEntity.ok(ApiResponseDto.success("사용자 정보를 성공적으로 조회했습니다", user)))
        .onErrorResume(
            error -> {
              log.error("사용자 정보 조회 실패", error);
              return Mono.just(
                  ResponseEntity.badRequest()
                      .body(ApiResponseDto.error("사용자 정보 조회에 실패했습니다: " + error.getMessage())));
            });
  }

  /** 메시지 수정 PUT /api/livechat/messages/{messageId} */
  @PutMapping("/messages/{messageId}")
  public Mono<ResponseEntity<ApiResponseDto<LiveChatResponseDto>>> updateMessage(
      @PathVariable String messageId, @Valid @RequestBody LiveChatRequestDto request) {

    log.info("메시지 수정 요청: messageId={}, content={}", messageId, request.getContent());

    // 추가: 외부 API에서 메시지 수정 기능 구현 시 연동
    return Mono.just(
            ResponseEntity.ok(
                ApiResponseDto.<LiveChatResponseDto>success("메시지 수정 기능은 준비 중입니다", null)))
        .onErrorResume(
            error -> {
              log.error("메시지 수정 실패", error);
              return Mono.just(
                  ResponseEntity.badRequest()
                      .body(
                          ApiResponseDto.<LiveChatResponseDto>error(
                              "메시지 수정에 실패했습니다: " + error.getMessage())));
            });
  }

  /** 메시지 삭제 DELETE /api/livechat/messages/{messageId} */
  @DeleteMapping("/messages/{messageId}")
  public Mono<ResponseEntity<ApiResponseDto<Void>>> deleteMessage(@PathVariable String messageId) {
    log.info("메시지 삭제 요청: messageId={}", messageId);

    // 추가: 외부 API에서 메시지 삭제 기능 구현 시 연동
    return Mono.just(ResponseEntity.ok(ApiResponseDto.<Void>success("메시지 삭제 기능은 준비 중입니다", null)))
        .onErrorResume(
            error -> {
              log.error("메시지 삭제 실패", error);
              return Mono.just(
                  ResponseEntity.badRequest()
                      .body(ApiResponseDto.<Void>error("메시지 삭제에 실패했습니다: " + error.getMessage())));
            });
  }

  /** 채팅방 참여자 목록 조회 GET /api/livechat/rooms/{roomId}/participants */
  @GetMapping("/rooms/{roomId}/participants")
  public Mono<ResponseEntity<ApiResponseDto<List<LiveChatUserDto>>>> getRoomParticipants(
      @PathVariable String roomId) {
    log.info("채팅방 참여자 목록 조회 요청: roomId={}", roomId);

    // 추가: 외부 API에서 참여자 목록 조회 기능 구현 시 연동
    return Mono.just(
            ResponseEntity.ok(
                ApiResponseDto.<List<LiveChatUserDto>>success("참여자 목록 조회 기능은 준비 중입니다", null)))
        .onErrorResume(
            error -> {
              log.error("참여자 목록 조회 실패", error);
              return Mono.just(
                  ResponseEntity.badRequest()
                      .body(
                          ApiResponseDto.<List<LiveChatUserDto>>error(
                              "참여자 목록 조회에 실패했습니다: " + error.getMessage())));
            });
  }
}
