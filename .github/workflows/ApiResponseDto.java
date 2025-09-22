/* (C) 2025 HMM Corp. All rights reserved. */
package com.hmm.cbui.domain.livechat.dto;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * LiveChat 외부Api 응답 Dto
 *
 * @author jhm
 * @since 2025.09.18
 */
@Getter
@Setter
@Builder
@ToString(callSuper = true)
@Schema(name = "LiveChat 외부Api 응답 Dto")
public class ApiResponseDto<T> {
  private boolean success;
  private String message;
  private T data;
  private String errorCode;
  private String errorMessage;
  private LocalDateTime timestamp;

  public static <T> ApiResponseDto<T> success(T data) {
    return ApiResponseDto.<T>builder()
        .success(true)
        .message("요청이 성공적으로 처리되었습니다")
        .data(data)
        .timestamp(LocalDateTime.now())
        .build();
  }

  public static <T> ApiResponseDto<T> success(String message, T data) {
    return ApiResponseDto.<T>builder()
        .success(true)
        .message(message)
        .data(data)
        .timestamp(LocalDateTime.now())
        .build();
  }

  public static <T> ApiResponseDto<T> error(String message) {
    return ApiResponseDto.<T>builder()
        .success(false)
        .message(message)
        .timestamp(LocalDateTime.now())
        .build();
  }

  public static <T> ApiResponseDto<T> error(String message, String errorCode) {
    return ApiResponseDto.<T>builder()
        .success(false)
        .message(message)
        .errorCode(errorCode)
        .timestamp(LocalDateTime.now())
        .build();
  }
}
