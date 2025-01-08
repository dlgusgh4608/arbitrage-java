package main.arbitrage.infrastructure.exchange.binance.priv.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import main.arbitrage.infrastructure.exchange.binance.dto.enums.BinanceEnums.MarginType;
import main.arbitrage.infrastructure.exchange.binance.dto.enums.BinanceEnums.Side;
import main.arbitrage.infrastructure.exchange.binance.dto.enums.BinanceEnums.Type;
import main.arbitrage.infrastructure.exchange.binance.dto.request.BinancePostOrderRequest;
import main.arbitrage.infrastructure.exchange.binance.dto.response.BinanceChangeLeverageResponse;
import main.arbitrage.infrastructure.exchange.binance.dto.response.BinanceGetAccountResponse;
import main.arbitrage.infrastructure.exchange.binance.dto.response.BinanceLeverageBracketResponse;
import main.arbitrage.infrastructure.exchange.binance.dto.response.BinanceOrderResponse;
import main.arbitrage.infrastructure.exchange.binance.dto.response.BinancePositionInfoResponse;
import main.arbitrage.infrastructure.exchange.binance.dto.response.BinanceSymbolInfoResponse;
import main.arbitrage.infrastructure.exchange.binance.exception.BinanceErrorCode;
import main.arbitrage.infrastructure.exchange.binance.exception.BinanceException;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class BinancePrivateRestService extends BaseBinancePrivateRestService {
  public BinancePrivateRestService(
      String accessKey,
      String secretKey,
      OkHttpClient okHttpClient,
      ObjectMapper objectMapper,
      List<String> symbolNames) {
    super(accessKey, secretKey, okHttpClient, objectMapper, symbolNames);
  }

  public List<BinanceGetAccountResponse> getAccount() {
    try {
      Map<String, Object> params = new LinkedHashMap<>();
      params.put("timestamp", System.currentTimeMillis());
      String queryString = generateToken(params);

      Request request =
          new Request.Builder()
              .url(DEFAULT_URL + "/v3/balance" + "?" + queryString)
              .addHeader("Content-Type", "application/json")
              .addHeader("X-MBX-APIKEY", accessKey)
              .get()
              .build();

      Response response = okHttpClient.newCall(request).execute();

      String responseBody = response.body().string();

      if (!response.isSuccessful()) validateResponse(objectMapper.readTree(responseBody));

      return objectMapper.readValue(
          responseBody,
          objectMapper
              .getTypeFactory()
              .constructCollectionType(List.class, BinanceGetAccountResponse.class));
    } catch (IOException e) {
      throw new BinanceException(BinanceErrorCode.API_ERROR, e);
    }
  }

  public Optional<BinanceGetAccountResponse> getUSDT() {
    List<BinanceGetAccountResponse> account = getAccount();
    return account.stream()
        .filter(binanceAccount -> binanceAccount.asset().equals("USDT"))
        .findFirst();
  }

  public BinanceOrderResponse order(
      String market, Side side, Type type, Double volume, Double price) {

    String paramString =
        String.format(
            "market=%s,side=%s,type=%s,price=%s,volume=%s", market, side, type, price, volume);

    try {
      if (market == null || side == null || type == null || volume == null) {
        throw new BinanceException(BinanceErrorCode.BAD_PARAMS, paramString);
      }

      if (type.equals(Type.LIMIT) && price == null) {
        throw new BinanceException(BinanceErrorCode.BAD_PARAMS, String.format(paramString));
      }

      String symbol = convertSymbol(market);

      BinancePostOrderRequest requestDto =
          BinancePostOrderRequest.builder()
              .newClientOrderId(UUID.randomUUID().toString())
              .type(type)
              .symbol(symbol)
              .side(side)
              .price(price)
              .quantity(volume)
              .build();

      Map<String, Object> map = objectMapper.convertValue(requestDto, LinkedHashMap.class);

      String queryString = generateToken(map);

      RequestBody body = RequestBody.create(new byte[0], null);

      String url = DEFAULT_URL + "/v1/order" + "?" + queryString;

      Request request =
          new Request.Builder()
              .url(url)
              .addHeader("Content-Type", "application/json")
              .addHeader("X-MBX-APIKEY", accessKey)
              .post(body)
              .build();

      Response response = okHttpClient.newCall(request).execute();

      String responseBody = response.body().string();

      if (!response.isSuccessful()) validateResponse(objectMapper.readTree(responseBody));

      return objectMapper.readValue(responseBody, BinanceOrderResponse.class);
    } catch (IOException e) {
      throw new BinanceException(BinanceErrorCode.API_ERROR, paramString, e);
    }
  }

  public BinanceSymbolInfoResponse symbolInfo(String symbolName) {
    try {
      String symbol = convertSymbol(symbolName);
      Map<String, Object> params = new LinkedHashMap<>();
      params.put("timestamp", System.currentTimeMillis());
      params.put("symbol", symbol);
      String queryString = generateToken(params);

      Request request =
          new Request.Builder()
              .url(DEFAULT_URL + "/v1/symbolConfig" + "?" + queryString)
              .addHeader("Content-Type", "application/json")
              .addHeader("X-MBX-APIKEY", accessKey)
              .get()
              .build();

      Response response = okHttpClient.newCall(request).execute();

      String responseBody = response.body().string();

      if (!response.isSuccessful()) validateResponse(objectMapper.readTree(responseBody));

      List<BinanceSymbolInfoResponse> responseList =
          objectMapper.readValue(
              responseBody,
              objectMapper
                  .getTypeFactory()
                  .constructCollectionType(List.class, BinanceSymbolInfoResponse.class));

      if (responseList.isEmpty()) return null;

      return responseList.get(0);
    } catch (IOException e) {
      throw new BinanceException(BinanceErrorCode.API_ERROR, e);
    }
  }

  public BinanceLeverageBracketResponse getLeverageBrackets(String symbolName) {
    try {
      String symbol = convertSymbol(symbolName);

      Map<String, Object> params = new LinkedHashMap<>();
      params.put("timestamp", System.currentTimeMillis());
      params.put("symbol", symbol);
      String queryString = generateToken(params);

      Request request =
          new Request.Builder()
              .url(DEFAULT_URL + "/v1/leverageBracket" + "?" + queryString)
              .addHeader("Content-Type", "application/json")
              .addHeader("X-MBX-APIKEY", accessKey)
              .get()
              .build();

      Response response = okHttpClient.newCall(request).execute();

      String responseBody = response.body().string();

      if (!response.isSuccessful()) validateResponse(objectMapper.readTree(responseBody));

      List<BinanceLeverageBracketResponse> responseList =
          objectMapper.readValue(
              responseBody,
              objectMapper
                  .getTypeFactory()
                  .constructCollectionType(List.class, BinanceLeverageBracketResponse.class));

      if (responseList.isEmpty()) return null;

      return responseList.get(0);
    } catch (IOException e) {
      throw new BinanceException(BinanceErrorCode.API_ERROR, e);
    }
  }

  public BinanceChangeLeverageResponse changeLeverage(String symbolName, int leverage) {
    try {
      String symbol = convertSymbol(symbolName);

      Map<String, Object> params = new LinkedHashMap<>();
      params.put("timestamp", System.currentTimeMillis());
      params.put("symbol", symbol);
      params.put("leverage", leverage);
      String queryString = generateToken(params);

      RequestBody body = RequestBody.create(new byte[0], null);

      Request request =
          new Request.Builder()
              .url(DEFAULT_URL + "/v1/leverage" + "?" + queryString)
              .addHeader("Content-Type", "application/json")
              .addHeader("X-MBX-APIKEY", accessKey)
              .post(body)
              .build();

      Response response = okHttpClient.newCall(request).execute();

      String responseBody = response.body().string();

      if (!response.isSuccessful()) validateResponse(objectMapper.readTree(responseBody));

      return objectMapper.readValue(responseBody, BinanceChangeLeverageResponse.class);
    } catch (IOException e) {
      throw new BinanceException(BinanceErrorCode.API_ERROR, e);
    }
  }

  public BinancePositionInfoResponse getPositionInfo(String symbolName) {
    try {
      String symbol = convertSymbol(symbolName);

      Map<String, Object> params = new LinkedHashMap<>();
      params.put("timestamp", System.currentTimeMillis());
      params.put("symbol", symbol);
      String queryString = generateToken(params);

      Request request =
          new Request.Builder()
              .url(DEFAULT_URL + "/v3/positionRisk" + "?" + queryString)
              .addHeader("Content-Type", "application/json")
              .addHeader("X-MBX-APIKEY", accessKey)
              .get()
              .build();

      Response response = okHttpClient.newCall(request).execute();

      String responseBody = response.body().string();

      if (!response.isSuccessful()) validateResponse(objectMapper.readTree(responseBody));

      List<BinancePositionInfoResponse> responseList =
          objectMapper.readValue(
              responseBody,
              objectMapper
                  .getTypeFactory()
                  .constructCollectionType(List.class, BinancePositionInfoResponse.class));

      if (responseList.isEmpty()) return null;

      return responseList.get(0);

    } catch (IOException e) {
      throw new BinanceException(BinanceErrorCode.API_ERROR, e);
    }
  }

  public boolean updateMarginType(String symbolName, MarginType marginType) {
    try {
      String symbol = convertSymbol(symbolName);

      Map<String, Object> params = new LinkedHashMap<>();
      params.put("timestamp", System.currentTimeMillis());
      params.put("symbol", symbol);
      params.put("marginType", marginType.name());
      String queryString = generateToken(params);

      RequestBody body = RequestBody.create(new byte[0], null);

      Request request =
          new Request.Builder()
              .url(DEFAULT_URL + "/v1/marginType" + "?" + queryString)
              .addHeader("Content-Type", "application/json")
              .addHeader("X-MBX-APIKEY", accessKey)
              .post(body)
              .build();

      Response response = okHttpClient.newCall(request).execute();

      String responseBody = response.body().string();

      if (!response.isSuccessful()) validateResponse(objectMapper.readTree(responseBody));

      return true;
    } catch (IOException e) {
      throw new BinanceException(BinanceErrorCode.API_ERROR, e);
    }
  }
}
