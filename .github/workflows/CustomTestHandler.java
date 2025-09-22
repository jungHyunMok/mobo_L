/* (C) 2025 HMM Corp. All rights reserved. */
package com.hmm.cbui.domain.test.websocket;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketSession;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import com.hmm.cbui.global.websocket.dto.StompFrame;
import com.hmm.cbui.global.websocket.handler.MessageHandler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

/** 커스텀 테스트 핸들러 destination: "/app/custom/**" 패턴의 메시지를 처리 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CustomTestHandler implements MessageHandler {

  private final SessionOutRegistry outRegistry;
  private final ObjectMapper objectMapper;

  @Override
  public boolean canHandle(String destination) {
    return destination != null && destination.startsWith("/app/custom/");
  }

  @Override
  public Mono<Void> handle(WebSocketSession session, StompFrame frame) {
    String destination = frame.getDestination();
    String sessionId = session.getId();

    log.info("CustomTestHandler 처리 중: destination={}, sessionId={}", destination, sessionId);

    return switch (destination) {
      case "/app/custom/test" -> handleTest(session, frame);
      case "/app/custom/echo" -> handleEcho(session, frame);
      case "/app/custom/broadcast" -> handleBroadcast(session, frame);
      default -> {
        emitError(sessionId, "지원하지 않는 destination: " + destination);
        yield Mono.empty();
      }
    };
  }

  private Mono<Void> handleTest(WebSocketSession session, StompFrame frame) {
    String sessionId = session.getId();

    // 테스트 응답 생성
    ObjectNode response = objectMapper.createObjectNode();
    response.put("type", "test_response");
    response.put("message", "커스텀 테스트 핸들러가 정상적으로 작동합니다!");
    response.put("sessionId", sessionId);
    response.put("timestamp", System.currentTimeMillis());
    response.set("originalBody", frame.getBody());

    outRegistry.emit(sessionId, response.toString());
    return Mono.empty();
  }

  private Mono<Void> handleEcho(WebSocketSession session, StompFrame frame) {
    String sessionId = session.getId();

    // 에코 응답 생성
    ObjectNode response = objectMapper.createObjectNode();
    response.put("type", "echo_response");
    response.put("message", "에코 메시지입니다.");
    response.put("sessionId", sessionId);
    response.put("timestamp", System.currentTimeMillis());
    response.set("echoBody", frame.getBody());

    outRegistry.emit(sessionId, response.toString());
    return Mono.empty();
  }

  private Mono<Void> handleBroadcast(WebSocketSession session, StompFrame frame) {
    String sessionId = session.getId();

    // 브로드캐스트 메시지 생성
    ObjectNode broadcastMessage = objectMapper.createObjectNode();
    broadcastMessage.put("type", "broadcast");
    broadcastMessage.put("message", "브로드캐스트 메시지입니다!");
    broadcastMessage.put("from", sessionId);
    broadcastMessage.put("timestamp", System.currentTimeMillis());
    broadcastMessage.set("data", frame.getBody());

    // 모든 세션에 브로드캐스트 ( 간단히 세션에만 응답 )
    outRegistry.emit(sessionId, broadcastMessage.toString());

    // 발신자에게 확인 메시지
    ObjectNode ackMessage = objectMapper.createObjectNode();
    ackMessage.put("type", "broadcast_ack");
    ackMessage.put("message", "브로드캐스트가 전송되었습니다.");
    ackMessage.put("timestamp", System.currentTimeMillis());

    outRegistry.emit(sessionId, ackMessage.toString());
    return Mono.empty();
  }

  private void emitError(String sessionId, String errorMessage) {
    ObjectNode errorResponse = objectMapper.createObjectNode();
    errorResponse.put("type", "error");
    errorResponse.put("message", errorMessage);
    errorResponse.put("timestamp", System.currentTimeMillis());

    outRegistry.emit(sessionId, errorResponse.toString());
  }
}
