package main.arbitrage.infrastructure.upbit;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import main.arbitrage.infrastructure.upbit.dto.enums.UpbitOrderEnums.OrdType;
import main.arbitrage.infrastructure.upbit.dto.enums.UpbitOrderEnums.Side;
import main.arbitrage.infrastructure.upbit.dto.enums.UpbitOrderEnums.State;
import main.arbitrage.infrastructure.upbit.dto.request.UpbitPostOrderRequest;
import main.arbitrage.infrastructure.upbit.dto.response.UpbitAccountResponse;
import main.arbitrage.infrastructure.upbit.dto.response.UpbitOrderResponse;
import main.arbitrage.infrastructure.upbit.exception.UpbitErrorCode;
import main.arbitrage.infrastructure.upbit.exception.UpbitException;

public class UpbitPrivateRestService extends BaseUpbitPrivateRestService {
  private final UpbitHttpInterface upbitClient;
  private final ObjectMapper objectMapper;

  public UpbitPrivateRestService(
      String accessKey,
      String secretKey,
      UpbitHttpInterface upbitClient,
      ObjectMapper objectMapper,
      List<String> symbolNames) {
    super(accessKey, secretKey, symbolNames);
    this.upbitClient = upbitClient;
    this.objectMapper = objectMapper;
  }

  public List<UpbitAccountResponse> getAccounts() {
    return upbitClient.getAccounts("Bearer " + generateToken());
  }

  public Optional<UpbitAccountResponse> getKRW() {
    List<UpbitAccountResponse> account = getAccounts();
    return account.stream()
        .filter(upbitAccount -> upbitAccount.currency().equals("KRW"))
        .findFirst();
  }

  public Optional<UpbitAccountResponse> getKRW(List<UpbitAccountResponse> originArray) {
    return getCurrentSymbol(originArray, "KRW");
  }

  public Optional<UpbitAccountResponse> getCurrentSymbol(
      List<UpbitAccountResponse> originArray, String symbolName) {
    return originArray.stream()
        .filter(upbitAccount -> upbitAccount.currency().equals(symbolName.toUpperCase()))
        .findFirst();
  }

  public String createOrder(String market, Side side, OrdType ordType, Long price, Double volume) {
    String paramString =
        String.format(
            "market=%s,side=%s,type=%s,price=%s,volume=%s",
            market, side.name(), ordType.name(), price, volume);

    if (market == null || side == null || ordType == null)
      throw new UpbitException(UpbitErrorCode.INVALID_PARAMETER, paramString);

    // orderType Validation
    switch (ordType) {
      case limit -> {
        if (volume == null || price == null)
          throw new UpbitException(UpbitErrorCode.INVALID_PARAMETER, paramString);
      }
      // 매수
      case price -> {
        if (price == null || side.equals(Side.ask))
          throw new UpbitException(UpbitErrorCode.INVALID_PARAMETER, paramString);
      }
      // 매도
      case market -> {
        if (volume == null || side.equals(Side.bid))
          throw new UpbitException(UpbitErrorCode.INVALID_PARAMETER, paramString);
      }
    }

    String symbol = convertSymbol(market);

    UpbitPostOrderRequest requestDto =
        UpbitPostOrderRequest.builder()
            .market(symbol)
            .side(side)
            .ordType(ordType)
            .price(price)
            .volume(volume)
            .build();

    Map<String, Object> body = objectMapper.convertValue(requestDto, Map.class);

    String token = generateToken(body);

    return upbitClient.createOrder("Bearer " + token, body).get("uuid").asText();
  }

  public UpbitOrderResponse getOrder(String uuid, int repeat) {
    try {
      if (repeat < 2) return null;
      Thread.sleep(1000);

      Map<String, Object> body = Map.of("uuid", uuid);

      String token = generateToken(body);

      UpbitOrderResponse response = upbitClient.getOrder("Bearer " + token, uuid);

      if (response.state() == State.wait || response.state() == State.watch)
        return getOrder(uuid, repeat - 1);

      return response;
    } catch (InterruptedException e) {
      throw new UpbitException(UpbitErrorCode.INTERRUPTED_ERROR, e);
    }
  }
}
