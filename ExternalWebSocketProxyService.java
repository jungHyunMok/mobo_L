/* (C) 2025 HMM Corp. All rights reserved. */
package com.hmm.cbui.domain.livechat.service;

import java.net.URI;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.springframework.stereotype.Service;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import org.springframework.web.reactive.socket.client.ReactorNettyWebSocketClient;
import org.springframework.web.reactive.socket.client.WebSocketClient;

import com.hmm.cbui.domain.livechat.dto.LiveChatMessageDto;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

/**
 * 외부 WebSocket 서버와의 프록시 연결을 관리하는 서비스 클라이언트와 외부 WebSocket 서버 간의 메시지를 중계합니다.
 *
 * @author jhm
 * @since 2025.09.18
 */
@Slf4j
@Service
public class ExternalWebSocketProxyService {

  private final WebSocketClient webSocketClient = new ReactorNettyWebSocketClient();
  private final ConcurrentMap<String, WebSocketSession> externalSessions =
      new ConcurrentHashMap<>();
  private final ConcurrentMap<String, Sinks.Many<LiveChatMessageDto>> messageSinks =
      new ConcurrentHashMap<>();

  /**
   * 외부 WebSocket 서버에 연결합니다.
   *
   * @param accessToken 외부 서버 인증 토큰
   * @param clientSession 클라이언트 WebSocket 세션
   * @return 연결 결과
   */
  public Mono<Void> connectToExternalServer(String accessToken, WebSocketSession clientSession) {
    String sessionId = clientSession.getId();

    try {
      String externalUrl = buildExternalWebSocketUrl(accessToken);
      URI uri = URI.create(externalUrl);

      log.info("외부 WebSocket 서버 연결 시도: sessionId={}, url={}", sessionId, externalUrl);

      return webSocketClient
          .execute(
              uri,
              externalSession -> {
                // 외부 세션을 저장
                externalSessions.put(sessionId, externalSession);
                log.info("외부 WebSocket 서버 연결 성공: sessionId={}", sessionId);

                // 외부 서버로부터 메시지 수신 처리
                return handleExternalConnection(externalSession, sessionId);
              })
          .doOnError(
              error -> {
                log.error("외부 WebSocket 서버 연결 실패: sessionId={}", sessionId, error);
                externalSessions.remove(sessionId);
              });

    } catch (Exception e) {
      log.error("외부 WebSocket URL 생성 실패: sessionId={}", sessionId, e);
      return Mono.error(e);
    }
  }

  /** 외부 WebSocket 연결을 처리합니다. */
  private Mono<Void> handleExternalConnection(
      WebSocketSession externalSession, String clientSessionId) {
    // 외부 서버로부터 메시지 수신
    return externalSession
        .receive()
        .flatMap(
            message -> {
              try {
                String payload = message.getPayloadAsText();
                log.debug(
                    "외부 서버로부터 메시지 수신: clientSessionId={}, message={}", clientSessionId, payload);

                // 메시지를 파싱하여 클라이언트에게 전달
                LiveChatMessageDto chatMessage = parseMessage(payload);
                if (chatMessage != null) {
                  Sinks.Many<LiveChatMessageDto> sink = messageSinks.get(clientSessionId);
                  if (sink != null) {
                    sink.tryEmitNext(chatMessage);
                  }
                }

                return Mono.empty();
              } catch (Exception e) {
                log.error("외부 메시지 처리 실패: clientSessionId={}", clientSessionId, e);
                return Mono.empty();
              }
            })
        .then();
  }

  /** 클라이언트로부터 외부 서버로 메시지를 전달합니다. */
  public Mono<Void> forwardMessageToExternal(String sessionId, LiveChatMessageDto message) {
    WebSocketSession externalSession = externalSessions.get(sessionId);
    if (externalSession == null) {
      log.warn("외부 세션을 찾을 수 없음: sessionId={}", sessionId);
      return Mono.empty();
    }

    try {
      String payload = serializeMessage(message);
      WebSocketMessage webSocketMessage = externalSession.textMessage(payload);

      return externalSession
          .send(Mono.just(webSocketMessage))
          .doOnSuccess(v -> log.debug("외부 서버로 메시지 전송 성공: sessionId={}", sessionId))
          .doOnError(error -> log.error("외부 서버로 메시지 전송 실패: sessionId={}", sessionId, error));

    } catch (Exception e) {
      log.error("메시지 직렬화 실패: sessionId={}", sessionId, e);
      return Mono.error(e);
    }
  }

  /** 외부 WebSocket 연결을 종료합니다. */
  public void disconnectExternalSession(String sessionId) {
    WebSocketSession externalSession = externalSessions.remove(sessionId);
    if (externalSession != null) {
      externalSession.close();
      log.info("외부 WebSocket 연결 종료: sessionId={}", sessionId);
    }

    Sinks.Many<LiveChatMessageDto> sink = messageSinks.remove(sessionId);
    if (sink != null) {
      sink.tryEmitComplete();
    }
  }

  /** 클라이언트 세션에 대한 메시지 싱크를 생성합니다. */
  public void createMessageSink(String sessionId) {
    Sinks.Many<LiveChatMessageDto> sink = Sinks.many().multicast().onBackpressureBuffer();
    messageSinks.put(sessionId, sink);
    log.debug("메시지 싱크 생성: sessionId={}", sessionId);
  }

  /** 클라이언트 세션의 메시지 스트림을 반환합니다. */
  public reactor.core.publisher.Flux<LiveChatMessageDto> getMessageStream(String sessionId) {
    Sinks.Many<LiveChatMessageDto> sink = messageSinks.get(sessionId);
    if (sink != null) {
      return sink.asFlux();
    }
    return reactor.core.publisher.Flux.empty();
  }

  /** 외부 WebSocket URL을 생성합니다. */
  private String buildExternalWebSocketUrl(String accessToken) {
    return "wss://dlchatlm.hmm21.com/websocket/060/gpbdnubi/websocket?access_token=" + accessToken;
  }

  /** 외부 서버 메시지를 파싱합니다. */
  private LiveChatMessageDto parseMessage(String payload) {
    try {
      // 실제 구현에서는 JSON 파싱 로직을 추가
      // 현재는 간단한 예시
      return LiveChatMessageDto.builder()
          .roomId("external")
          .sender("external")
          .content(payload)
          .timestamp(String.valueOf(System.currentTimeMillis()))
          .build();
    } catch (Exception e) {
      log.error("메시지 파싱 실패: payload={}", payload, e);
      return null;
    }
  }

  /** 메시지를 직렬화합니다. */
  private String serializeMessage(LiveChatMessageDto message) {
    // 실제 구현에서는 JSON 직렬화 로직을 추가
    return String.format(
        "{\"roomId\":\"%s\",\"sender\":\"%s\",\"content\":\"%s\",\"timestamp\":\"%s\"}",
        message.getRoomId(), message.getSender(), message.getContent(), message.getTimestamp());
  }
}
