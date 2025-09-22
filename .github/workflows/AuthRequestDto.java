/* (C) 2025 HMM Corp. All rights reserved. */
package com.hmm.cbui.domain.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Auth 요청 DTO
 *
 * @author jhm
 * @since 2025.01.20
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "인증 요청 DTO")
public class AuthRequestDto {

  @NotBlank(message = "토큰은 필수입니다")
  @Schema(description = "클라이언트에서 전달받은 토큰", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
  private String token;

  @Schema(description = "사용자 ID (선택사항)", example = "user123")
  private String userId;
}
