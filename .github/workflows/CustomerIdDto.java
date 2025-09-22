/* (C) 2025 HMM Corp. All rights reserved. */
package com.hmm.cbui.domain.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Customer ID DTO
 *
 * @author jhm
 * @since 2025.01.20
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "Customer ID DTO")
public class CustomerIdDto {

  @Schema(description = "생성된 Customer ID", example = "customer_12345")
  private String customerId;

  @Schema(description = "로그인 ID", example = "tester_ts@hmm21.com")
  private String loginId;

  @Schema(description = "생성 상태", example = "SUCCESS")
  private String status;
}
