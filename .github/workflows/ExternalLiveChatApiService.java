/* (C) 2025 HMM Corp. All rights reserved. */
package com.hmm.cbui.domain.livechat.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.hmm.cbui.domain.livechat.dto.LiveChatRequestDto;
import com.hmm.cbui.domain.livechat.dto.LiveChatResponseDto;
import com.hmm.cbui.domain.livechat.dto.LiveChatRoomDto;
import com.hmm.cbui.domain.livechat.dto.LiveChatUserDto;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

/**
 * LiveChat External API 라이브챗관련 Mocking 모듈 서비스 ( 실제 외부 API 연동시 구현 )
 *
 * @author jhm
 * @since 2025.09.18
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ExternalLiveChatApiService {

  private final WebClient webClient;

  // 외부 API 기본 URL (실제 연동 시 설정)
  private static final String EXTERNAL_API_BASE_URL = "https://external-livechat-api.com/api/v1";

  /** 외부 API로 메시지 전송 */
  public Mono<LiveChatResponseDto> sendMessage(LiveChatRequestDto request) {
    log.info("외부 API로 메시지 전송 요청: roomId={}, sender={}", request.getRoomId(), request.getSender());

    //  실제 외부 API 호출
    // return webClient.post()
    //     .uri(EXTERNAL_API_BASE_URL + "/messages")
    //     .bodyValue(request)
    //     .retrieve()
    //     .bodyToMono(LiveChatResponseDto.class);

    // 현재는 모킹된 응답 반환
    return Mono.just(mockSendMessageResponse(request));
  }

  /** 외부 API에서 메시지 목록 조회 */
  public Mono<List<LiveChatResponseDto>> getMessages(String roomId, int limit, int offset) {
    log.info("외부 API에서 메시지 목록 조회: roomId={}, limit={}, offset={}", roomId, limit, offset);

    //  실제 외부 API 호출
    // return webClient.get()
    //     .uri(EXTERNAL_API_BASE_URL + "/rooms/{roomId}/messages?limit={limit}&offset={offset}",
    // roomId, limit, offset)
    //     .retrieve()
    //     .bodyToFlux(LiveChatResponseDto.class)
    //     .collectList();

    // 현재는 모킹된 응답 반환
    return Mono.just(mockGetMessagesResponse(roomId, limit));
  }

  /** 외부 API에서 채팅방 정보 조회 */
  public Mono<LiveChatRoomDto> getRoomInfo(String roomId) {
    log.info("외부 API에서 채팅방 정보 조회: roomId={}", roomId);

    //  실제 외부 API 호출
    // return webClient.get()
    //     .uri(EXTERNAL_API_BASE_URL + "/rooms/{roomId}", roomId)
    //     .retrieve()
    //     .bodyToMono(LiveChatRoomDto.class);

    // 현재는 모킹된 응답 반환
    return Mono.just(mockGetRoomInfoResponse(roomId));
  }

  /** 외부 API에서 사용자 정보 조회 */
  public Mono<LiveChatUserDto> getUserInfo(String userId) {
    log.info("외부 API에서 사용자 정보 조회: userId={}", userId);

    // 실제 외부 API 호출
    // return webClient.get()
    //     .uri(EXTERNAL_API_BASE_URL + "/users/{userId}", userId)
    //     .retrieve()
    //     .bodyToMono(LiveChatUserDto.class);

    // 현재는 모킹된 응답 반환
    return Mono.just(mockGetUserInfoResponse(userId));
  }

  /** 외부 API로 채팅방 생성 */
  public Mono<LiveChatRoomDto> createRoom(LiveChatRoomDto roomRequest) {
    log.info("외부 API로 채팅방 생성 요청: roomName={}", roomRequest.getRoomName());

    //  실제 외부 API 호출
    // return webClient.post()
    //     .uri(EXTERNAL_API_BASE_URL + "/rooms")
    //     .bodyValue(roomRequest)
    //     .retrieve()
    //     .bodyToMono(LiveChatRoomDto.class);

    // 현재는 모킹된 응답 반환
    return Mono.just(mockCreateRoomResponse(roomRequest));
  }

  // ========== 모킹 메서드들 ==========

  private LiveChatResponseDto mockSendMessageResponse(LiveChatRequestDto request) {
    return LiveChatResponseDto.builder()
        .messageId(UUID.randomUUID().toString())
        .roomId(request.getRoomId())
        .sender(request.getSender())
        .content(request.getContent())
        .messageType(request.getMessageType() != null ? request.getMessageType() : "TEXT")
        .timestamp(LocalDateTime.now())
        .status("SENT")
        .replyTo(request.getReplyTo())
        .metadata(request.getMetadata())
        .isEdited(false)
        .build();
  }

  private List<LiveChatResponseDto> mockGetMessagesResponse(String roomId, int limit) {
    return List.of(
        LiveChatResponseDto.builder()
            .messageId(UUID.randomUUID().toString())
            .roomId(roomId)
            .sender("user1")
            .content("안녕하세요! 라이브챗 테스트입니다.")
            .messageType("TEXT")
            .timestamp(LocalDateTime.now().minusMinutes(5))
            .status("READ")
            .isEdited(false)
            .build(),
        LiveChatResponseDto.builder()
            .messageId(UUID.randomUUID().toString())
            .roomId(roomId)
            .sender("user2")
            .content("네, 안녕하세요! 잘 작동하고 있네요.")
            .messageType("TEXT")
            .timestamp(LocalDateTime.now().minusMinutes(3))
            .status("READ")
            .isEdited(false)
            .build());
  }

  private LiveChatRoomDto mockGetRoomInfoResponse(String roomId) {
    return LiveChatRoomDto.builder()
        .roomId(roomId)
        .roomName("테스트 채팅방")
        .description("라이브챗 API 테스트용 채팅방입니다")
        .status("ACTIVE")
        .createdBy("admin")
        .createdAt(LocalDateTime.now().minusDays(1))
        .updatedAt(LocalDateTime.now().minusMinutes(10))
        .participants(List.of("user1", "user2", "user3"))
        .participantCount(3)
        .lastMessage("네, 안녕하세요! 잘 작동하고 있네요.")
        .lastMessageAt(LocalDateTime.now().minusMinutes(3))
        .build();
  }

  private LiveChatUserDto mockGetUserInfoResponse(String userId) {
    return LiveChatUserDto.builder()
        .userId(userId)
        .userName("user" + userId)
        .displayName("사용자 " + userId)
        .email("user" + userId + "@example.com")
        .avatar("https://example.com/avatar/" + userId + ".jpg")
        .status("ONLINE")
        .lastSeen(LocalDateTime.now().minusMinutes(1))
        .role("USER")
        .isActive(true)
        .build();
  }

  private LiveChatRoomDto mockCreateRoomResponse(LiveChatRoomDto roomRequest) {
    return LiveChatRoomDto.builder()
        .roomId(UUID.randomUUID().toString())
        .roomName(roomRequest.getRoomName())
        .description(roomRequest.getDescription())
        .status("ACTIVE")
        .createdBy(roomRequest.getCreatedBy())
        .createdAt(LocalDateTime.now())
        .updatedAt(LocalDateTime.now())
        .participants(List.of(roomRequest.getCreatedBy()))
        .participantCount(1)
        .build();
  }
}
