package main.arbitrage.infrastructure.exchange.binance.pub.rest;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import main.arbitrage.domain.symbol.service.SymbolVariableService;
import main.arbitrage.infrastructure.exchange.binance.dto.response.BinanceExchangeInfoResponse;
import main.arbitrage.infrastructure.exchange.binance.exception.BinanceErrorCode;
import main.arbitrage.infrastructure.exchange.binance.exception.BinanceException;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.stereotype.Service;

@Service
public class BinancePublicRestService extends BaseBinancePublicRestService {

  public BinancePublicRestService(
      OkHttpClient okHttpClient,
      ObjectMapper objectMapper,
      SymbolVariableService symbolVariableService) {
    super(okHttpClient, objectMapper, symbolVariableService);
  }

  public Map<String, BinanceExchangeInfoResponse> getExchangeInfo() {
    try {
      String url = DEFAULT_URL + "/v1/exchangeInfo";

      Request request =
          new Request.Builder()
              .url(url)
              .addHeader("Content-Type", "application/json")
              .get()
              .build();

      Response response = okHttpClient.newCall(request).execute();

      String responseBody = response.body().string();

      if (!response.isSuccessful()) objectMapper.readTree(responseBody);

      JsonNode json = objectMapper.readTree(responseBody);

      System.out.println(json.toString());

      JsonNode symbols = json.get("symbols");

      Map<String, BinanceExchangeInfoResponse> exchangeHashMap = new HashMap<>();

      String MARKET_LOT_SIZE = "MARKET_LOT_SIZE"; // 시장가 체결 필터
      String MIN_NOTIONAL = "MIN_NOTIONAL"; // 최소 주문금액 필터
      String PRICE_FILTER = "PRICE_FILTER"; // 가격 필터

      for (JsonNode symbol : symbols) {
        String symbolName = symbol.get("baseAsset").asText();
        String symbolFullName = symbol.get("symbol").asText();

        if (symbolVariableService.isSupportedSymbol(symbolFullName.replace("USDT", ""))) {
          JsonNode filters = symbol.get("filters");

          Double maxQty = null;
          Double minQty = null;
          Double stepSize = null;
          Double minUsdt = null;
          Double tickSize = null;

          for (JsonNode filter : filters) {
            String filterType = filter.get("filterType").asText();

            if (MARKET_LOT_SIZE.equals(filterType)) {
              maxQty = filter.get("maxQty").asDouble();
              minQty = filter.get("minQty").asDouble();
              stepSize = filter.get("stepSize").asDouble();
            } else if (MIN_NOTIONAL.equals(filterType)) {
              minUsdt = filter.get("notional").asDouble();
            } else if (PRICE_FILTER.equals(filterType)) {
              tickSize = filter.get("tickSize").asDouble();
            }
          }

          exchangeHashMap.put(
              symbolName,
              new BinanceExchangeInfoResponse(maxQty, minQty, stepSize, minUsdt, tickSize));
        }
      }
      return exchangeHashMap;
    } catch (IOException e) {
      throw new BinanceException(BinanceErrorCode.API_ERROR, e);
    }
  }
}
