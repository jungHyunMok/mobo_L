/* (C) 2025 HMM Corp. All rights reserved. */
package com.hmm.cbui.domain.livechat.service;

import org.springframework.stereotype.Service;

import com.hmm.cbui.domain.livechat.dto.LiveChatMessageDto;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 라이브채팅 서비스 Redis 메시지 브로커를 사용하여 메시지를 관리합니다.
 *
 * @author jhm
 * @since 2025.09.18
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LiveChatService {

  private final RedisMessageBrokerService messageBrokerService;

  /** 특정 채팅방에 메시지를 발행합니다. */
  public void publish(String roomId, LiveChatMessageDto message) {
    log.info(
        "메시지 발행: roomId={}, sender={}, content={}",
        roomId,
        message.getSender(),
        message.getContent());

    messageBrokerService.publishToRoom(roomId, message);
  }

  /** 특정 채팅방을 구독합니다. */
  public void subscribe(String roomId) {
    log.info("채팅방 구독: roomId={}", roomId);
    messageBrokerService.subscribeToRoom(roomId);
  }

  /** 특정 채팅방 구독을 해제합니다. */
  public void unsubscribe(String roomId) {
    log.info("채팅방 구독 해제: roomId={}", roomId);
    messageBrokerService.unsubscribeFromRoom(roomId);
  }

  /** 특정 채팅방의 메시지 스트림을 반환합니다. */
  public reactor.core.publisher.Flux<LiveChatMessageDto> getMessageStream(String roomId) {
    return messageBrokerService.getMessageStream(roomId);
  }
}
