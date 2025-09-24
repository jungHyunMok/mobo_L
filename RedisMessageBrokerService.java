/* (C) 2025 HMM Corp. All rights reserved. */
package com.hmm.cbui.domain.livechat.service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.hmm.cbui.domain.livechat.dto.LiveChatMessageDto;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Sinks;

/**
 * Redis를 이용한 메시지 브로커 서비스 채팅방별 메시지 구독/발행을 관리합니다.
 *
 * @author jhm
 * @since 2025.09.18
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RedisMessageBrokerService {

  private final RedisTemplate<String, Object> redisTemplate;
  private final RedisMessageListenerContainer messageListenerContainer;
  private final ObjectMapper objectMapper;

  private final ConcurrentMap<String, Sinks.Many<LiveChatMessageDto>> roomSinks =
      new ConcurrentHashMap<>();
  private final ConcurrentMap<String, ChannelTopic> roomTopics = new ConcurrentHashMap<>();

  /** 특정 채팅방의 메시지를 구독합니다. */
  public void subscribeToRoom(String roomId) {
    log.info("채팅방 구독 시작: roomId={}", roomId);

    ChannelTopic topic = getOrCreateTopic(roomId);
    Sinks.Many<LiveChatMessageDto> sink = getOrCreateSink(roomId);

    // Redis 채널 구독
    MessageListenerAdapter adapter =
        new MessageListenerAdapter(
            new Object() {
              public void handleMessage(byte[] message) {
                try {
                  String payload = new String(message);
                  LiveChatMessageDto chatMessage =
                      objectMapper.readValue(payload, LiveChatMessageDto.class);
                  sink.tryEmitNext(chatMessage);
                  log.debug(
                      "Redis에서 메시지 수신: roomId={}, content={}", roomId, chatMessage.getContent());
                } catch (Exception e) {
                  log.error("Redis 메시지 처리 실패: roomId={}", roomId, e);
                }
              }
            });

    messageListenerContainer.addMessageListener(adapter, topic);
  }

  /** 특정 채팅방에 메시지를 발행합니다. */
  public void publishToRoom(String roomId, LiveChatMessageDto message) {
    log.info("채팅방에 메시지 발행: roomId={}, sender={}", roomId, message.getSender());

    try {
      String payload = objectMapper.writeValueAsString(message);
      ChannelTopic topic = getOrCreateTopic(roomId);

      redisTemplate.convertAndSend(topic.getTopic(), payload);
      log.debug("Redis로 메시지 발행 완료: roomId={}", roomId);

    } catch (Exception e) {
      log.error("Redis 메시지 발행 실패: roomId={}", roomId, e);
    }
  }

  /** 채팅방 구독을 해제합니다. */
  public void unsubscribeFromRoom(String roomId) {
    log.info("채팅방 구독 해제: roomId={}", roomId);

    ChannelTopic topic = roomTopics.remove(roomId);
    if (topic != null) {
      // Redis 채널 구독 해제 로직 추가
      log.debug("Redis 채널 구독 해제: roomId={}", roomId);
    }

    Sinks.Many<LiveChatMessageDto> sink = roomSinks.remove(roomId);
    if (sink != null) {
      sink.tryEmitComplete();
    }
  }

  /** 특정 채팅방의 메시지 스트림을 반환합니다. */
  public reactor.core.publisher.Flux<LiveChatMessageDto> getMessageStream(String roomId) {
    Sinks.Many<LiveChatMessageDto> sink = getOrCreateSink(roomId);
    return sink.asFlux();
  }

  /** 채팅방 토픽을 가져오거나 생성합니다. */
  private ChannelTopic getOrCreateTopic(String roomId) {
    return roomTopics.computeIfAbsent(roomId, key -> new ChannelTopic("livechat:room:" + key));
  }

  /** 채팅방 싱크를 가져오거나 생성합니다. */
  private Sinks.Many<LiveChatMessageDto> getOrCreateSink(String roomId) {
    return roomSinks.computeIfAbsent(
        roomId, key -> Sinks.many().multicast().onBackpressureBuffer());
  }

  /** 모든 채팅방 구독을 해제합니다. */
  public void unsubscribeAll() {
    log.info("모든 채팅방 구독 해제");

    roomTopics.keySet().forEach(this::unsubscribeFromRoom);
    roomSinks.clear();
    roomTopics.clear();
  }
}
