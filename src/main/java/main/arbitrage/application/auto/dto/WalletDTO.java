package main.arbitrage.application.auto.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class WalletDTO {
  private double krw;
  private double usdt;
}
