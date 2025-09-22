/* (C) 2025 HMM Corp. All rights reserved. */
package com.hmm.cbui.domain.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * LiveChat 토큰 DTO
 *
 * @author jhm
 * @since 2025.01.20
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "LiveChat 토큰 DTO")
public class LiveChatTokenDto {

  @Schema(description = "LiveChat에서 발급받은 토큰", example = "livechat_token_12345")
  private String token;

  @Schema(description = "토큰 유효기간 (초)", example = "3600")
  private Long expiresIn;

  @Schema(description = "토큰 타입", example = "Bearer")
  private String tokenType;
}
