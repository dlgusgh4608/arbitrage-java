package main.arbitrage.application.auto.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class WalletDTO {
  private double krw;
  private double usdt;
}
