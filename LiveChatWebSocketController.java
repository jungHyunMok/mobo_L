/* (C) 2025 HMM Corp. All rights reserved. */
package com.hmm.cbui.domain.livechat.controller;

import java.security.Principal;

import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import com.hmm.cbui.domain.livechat.dto.LiveChatMessageDto;
import com.hmm.cbui.domain.livechat.service.ExternalWebSocketProxyService;
import com.hmm.cbui.domain.livechat.service.LiveChatService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 라이브채팅 WebSocket 메시지 처리 컨트롤러 STOMP 프로토콜을 사용하여 실시간 메시지를 처리합니다.
 *
 * @author jhm
 * @since 2025.09.18
 */
@Slf4j
@Controller
@RequiredArgsConstructor
public class LiveChatWebSocketController {

  private final SimpMessagingTemplate messagingTemplate;
  private final LiveChatService liveChatService;
  private final ExternalWebSocketProxyService proxyService;

  /** 채팅방에 메시지 전송 클라이언트에서 /app/livechat/rooms/{roomId}/send 로 메시지를 보내면 처리됩니다. */
  @MessageMapping("/livechat/rooms/{roomId}/send")
  public void sendMessage(
      @DestinationVariable String roomId,
      @Payload LiveChatMessageDto message,
      Principal principal,
      SimpMessageHeaderAccessor headerAccessor) {

    log.info(
        "메시지 전송 요청: roomId={}, sender={}, content={}",
        roomId,
        message.getSender(),
        message.getContent());

    // 메시지에 타임스탬프 추가
    message.setTimestamp(String.valueOf(System.currentTimeMillis()));
    message.setRoomId(roomId);

    // Redis를 통한 메시지 브로드캐스트
    liveChatService.publish(roomId, message);

    // 특정 방의 모든 구독자에게 메시지 전송
    messagingTemplate.convertAndSend("/topic/livechat/rooms/" + roomId, message);

    // 외부 WebSocket 서버로 메시지 전달 (필요한 경우)
    String sessionId = headerAccessor.getSessionId();
    if (sessionId != null) {
      proxyService
          .forwardMessageToExternal(sessionId, message)
          .subscribe(null, error -> log.error("외부 서버로 메시지 전달 실패: sessionId={}", sessionId, error));
    }
  }

  /** 채팅방 구독 클라이언트에서 /app/livechat/rooms/{roomId}/subscribe 로 구독 요청을 보내면 처리됩니다. */
  @MessageMapping("/livechat/rooms/{roomId}/subscribe")
  public void subscribeToRoom(
      @DestinationVariable String roomId,
      Principal principal,
      SimpMessageHeaderAccessor headerAccessor) {

    log.info("채팅방 구독 요청: roomId={}, user={}", roomId, principal.getName());

    // 구독 확인 메시지 전송
    LiveChatMessageDto subscribeMessage =
        LiveChatMessageDto.builder()
            .roomId(roomId)
            .sender("system")
            .content("채팅방에 참여했습니다.")
            .timestamp(String.valueOf(System.currentTimeMillis()))
            .build();

    messagingTemplate.convertAndSendToUser(
        principal.getName(), "/queue/livechat/rooms/" + roomId, subscribeMessage);
  }

  /** 사용자 상태 업데이트 클라이언트에서 /app/livechat/users/{userId}/status 로 상태 업데이트를 보내면 처리됩니다. */
  @MessageMapping("/livechat/users/{userId}/status")
  public void updateUserStatus(
      @DestinationVariable String userId, @Payload String status, Principal principal) {

    log.info("사용자 상태 업데이트: userId={}, status={}", userId, status);

    // 상태 업데이트 메시지 생성
    LiveChatMessageDto statusMessage =
        LiveChatMessageDto.builder()
            .roomId("system")
            .sender("system")
            .content(userId + "님이 " + status + " 상태로 변경되었습니다.")
            .timestamp(String.valueOf(System.currentTimeMillis()))
            .build();

    // 모든 구독자에게 상태 업데이트 알림
    messagingTemplate.convertAndSend("/topic/livechat/status", statusMessage);
  }
}
