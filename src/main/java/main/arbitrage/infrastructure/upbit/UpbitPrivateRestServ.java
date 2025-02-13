package main.arbitrage.infrastructure.upbit;

import java.util.List;
import java.util.Map;
import main.arbitrage.infrastructure.exchange.upbit.dto.response.UpbitAccountResponse;

public class UpbitPrivateRestServ extends BaseUpbitPrivateRestServ {
  private final UpbitHttpInterface upbitClient;

  public UpbitPrivateRestServ(
      String accessKey,
      String secretKey,
      List<String> symbolNames,
      UpbitHttpInterface upbitClient) {
    super(accessKey, secretKey, symbolNames);
    this.upbitClient = upbitClient;
  }

  public List<UpbitAccountResponse> getAccounts() {
    return upbitClient.getAccounts("Bearer " + generateToken(), Map.of("test", "hello world"));
  }
}
