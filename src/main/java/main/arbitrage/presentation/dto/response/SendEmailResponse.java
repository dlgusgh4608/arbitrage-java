package main.arbitrage.presentation.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SendEmailResponse {
  private final String code;
}
