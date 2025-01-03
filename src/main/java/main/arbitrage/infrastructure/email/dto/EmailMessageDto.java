package main.arbitrage.infrastructure.email.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class EmailMessageDTO {
  private final String to;
  private final String subject;
  private final String message;
}
