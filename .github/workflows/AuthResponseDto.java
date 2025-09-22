/* (C) 2025 HMM Corp. All rights reserved. */
package com.hmm.cbui.domain.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Auth 응답 DTO
 *
 * @author jhm
 * @since 2025.01.20
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "인증 응답 DTO")
public class AuthResponseDto {

  @Schema(description = "토큰 검증 결과", example = "true")
  private Boolean isValid;

  @Schema(description = "사용자 ID", example = "user123")
  private String userId;

  @Schema(description = "사용자 이름", example = "홍길동")
  private String userName;

  @Schema(description = "LiveChat 토큰 정보")
  private LiveChatTokenDto liveChatToken;

  @Schema(description = "Customer ID 정보")
  private CustomerIdDto customerId;

  @Schema(
      description = "WebSocket 연결 URL",
      example = "wss://dlchatlm.hmm21.com/websocket/060/gpbdnubi/websocket?access_token=...")
  private String websocketUrl;

  @Schema(description = "인증 성공 여부", example = "true")
  private Boolean authSuccess;
}
