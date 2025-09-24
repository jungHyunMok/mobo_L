/* (C) 2025 HMM Corp. All rights reserved. */
package com.hmm.cbui.domain.livechat.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class LiveChatMessageDto {
  private String roomId;
  private String sender;
  private String content;
  private String timestamp;
}
