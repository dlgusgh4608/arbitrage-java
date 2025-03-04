package main.arbitrage.presentation.dto.message;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Builder
@Getter
@ToString
public class MessageResponse {
  private final boolean isSuccess;
  private final String message;
}
