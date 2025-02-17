package main.arbitrage.infrastructure.binance;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import main.arbitrage.domain.symbol.service.SymbolVariableService;
import main.arbitrage.infrastructure.binance.dto.response.BinanceExchangeInfoResponse;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BinancePublicRestService {
  private final SymbolVariableService symbolVariableService;
  private final BinanceClient binanceClient;

  public Map<String, BinanceExchangeInfoResponse> getExchangeInfo() {
    JsonNode json = binanceClient.getExchangeInfo();

    JsonNode symbols = json.get("symbols");

    if (symbols == null) return null;

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
  }
}
