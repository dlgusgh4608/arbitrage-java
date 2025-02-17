package main.arbitrage.infrastructure.binance;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import main.arbitrage.global.util.regex.KeyGenUtil;
import main.arbitrage.infrastructure.binance.dto.enums.BinanceEnums.MarginType;
import main.arbitrage.infrastructure.binance.dto.enums.BinanceEnums.Side;
import main.arbitrage.infrastructure.binance.dto.enums.BinanceEnums.Type;
import main.arbitrage.infrastructure.binance.dto.request.BinancePostOrderRequest;
import main.arbitrage.infrastructure.binance.dto.response.BinanceAccountResponse;
import main.arbitrage.infrastructure.binance.dto.response.BinanceChangeLeverageResponse;
import main.arbitrage.infrastructure.binance.dto.response.BinanceLeverageBracketResponse;
import main.arbitrage.infrastructure.binance.dto.response.BinanceOrderResponse;
import main.arbitrage.infrastructure.binance.dto.response.BinancePositionInfoResponse;
import main.arbitrage.infrastructure.binance.dto.response.BinanceSymbolInfoResponse;
import main.arbitrage.infrastructure.binance.exception.BinanceErrorCode;
import main.arbitrage.infrastructure.binance.exception.BinanceException;

public class BinancePrivateRestService extends BaseBinancePrivateRestService {
  private final BinanceClient binanceClient;
  private final ObjectMapper objectMapper;

  public BinancePrivateRestService(
      String accessKey,
      String secretKey,
      BinanceClient binanceClient,
      ObjectMapper objectMapper,
      List<String> symbolNames) {
    super(accessKey, secretKey, symbolNames);
    this.binanceClient = binanceClient;
    this.objectMapper = objectMapper;
  }

  private void validateOrder(String market, Side side, Type type, Double volume, Double price) {
    String paramString =
        String.format(
            "market=%s,side=%s,type=%s,price=%s,volume=%s", market, side, type, price, volume);

    if (market == null || side == null || type == null || volume == null)
      throw new BinanceException(BinanceErrorCode.BAD_PARAMS, paramString);

    if (type.equals(Type.LIMIT) && price == null)
      throw new BinanceException(BinanceErrorCode.BAD_PARAMS, paramString);
  }

  public List<BinanceAccountResponse> getAccount() {
    Map<String, Object> params = new LinkedHashMap<>();
    params.put("timestamp", System.currentTimeMillis());
    generateToken(params);

    return binanceClient.getAccounts(accessKey, params);
  }

  public Optional<BinanceAccountResponse> getUSDT() {
    List<BinanceAccountResponse> account = getAccount();
    return account.stream()
        .filter(binanceAccount -> binanceAccount.asset().equals("USDT"))
        .findFirst();
  }

  public BinanceOrderResponse createOrder(
      String market, Side side, Type type, Double volume, Double price, boolean isAuto) {

    validateOrder(market, side, type, volume, price);
    String symbol = convertSymbol(market);

    BinancePostOrderRequest requestDto =
        BinancePostOrderRequest.builder()
            .newClientOrderId(isAuto ? KeyGenUtil.generate() : UUID.randomUUID().toString())
            .type(type)
            .symbol(symbol)
            .side(side)
            .price(price)
            .quantity(volume)
            .build();

    Map<String, Object> map = objectMapper.convertValue(requestDto, LinkedHashMap.class);

    generateToken(map);

    return binanceClient.createOrder(accessKey, map);
  }

  public BinanceOrderResponse cancelOrder(String symbolName, String clientId) {
    String symbol = convertSymbol(symbolName);
    Map<String, Object> params = new LinkedHashMap<>();
    params.put("timestamp", System.currentTimeMillis());
    params.put("symbol", symbol);
    params.put("origClientOrderId", clientId);
    generateToken(params);

    return binanceClient.cancelOrder(accessKey, params);
  }

  public BinanceSymbolInfoResponse symbolInfo(String symbolName) {
    String symbol = convertSymbol(symbolName);
    Map<String, Object> params = new LinkedHashMap<>();
    params.put("timestamp", System.currentTimeMillis());
    params.put("symbol", symbol);
    generateToken(params);

    List<BinanceSymbolInfoResponse> response = binanceClient.getSymbolInfo(accessKey, params);

    if (response.isEmpty()) return null;

    return response.get(0);
  }

  public BinanceLeverageBracketResponse getLeverageBrackets(String symbolName) {
    String symbol = convertSymbol(symbolName);

    Map<String, Object> params = new LinkedHashMap<>();
    params.put("timestamp", System.currentTimeMillis());
    params.put("symbol", symbol);
    generateToken(params);

    List<BinanceLeverageBracketResponse> response =
        binanceClient.getLeverageBrackets(accessKey, params);

    if (response.isEmpty()) return null;

    return response.get(0);
  }

  public BinanceChangeLeverageResponse changeLeverage(String symbolName, int leverage) {
    String symbol = convertSymbol(symbolName);
    Map<String, Object> params = new LinkedHashMap<>();
    params.put("timestamp", System.currentTimeMillis());
    params.put("symbol", symbol);
    params.put("leverage", leverage);
    generateToken(params);

    return binanceClient.updateLeverage(accessKey, params);
  }

  public BinancePositionInfoResponse getPositionInfo(String symbolName) {
    String symbol = convertSymbol(symbolName);

    Map<String, Object> params = new LinkedHashMap<>();
    params.put("timestamp", System.currentTimeMillis());
    params.put("symbol", symbol);
    generateToken(params);

    List<BinancePositionInfoResponse> response = binanceClient.getPositionInfo(accessKey, params);

    if (response.isEmpty()) return null;

    return response.get(0);
  }

  public boolean updateMarginType(String symbolName, MarginType marginType) {
    String symbol = convertSymbol(symbolName);

    Map<String, Object> params = new LinkedHashMap<>();
    params.put("timestamp", System.currentTimeMillis());
    params.put("symbol", symbol);
    params.put("marginType", marginType.name());
    generateToken(params);

    return binanceClient.updateMarginType(accessKey, params).get("code").asInt() == 200;
  }

  public String createListenKey() {
    Map<String, Object> params = new LinkedHashMap<>();
    generateToken(params);
    return binanceClient.createListenKey(accessKey, params).get("listenKey").asText();
  }

  public String updateListenKey() {
    Map<String, Object> params = new LinkedHashMap<>();
    generateToken(params);
    return binanceClient.updateListenKey(accessKey, params).get("listenKey").asText();
  }
}
