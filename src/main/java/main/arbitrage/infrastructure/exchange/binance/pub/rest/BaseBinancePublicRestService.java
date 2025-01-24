package main.arbitrage.infrastructure.exchange.binance.pub.rest;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import java.util.stream.Collectors;
import main.arbitrage.domain.symbol.service.SymbolVariableService;
import main.arbitrage.infrastructure.exchange.ExchangeRestService;
import main.arbitrage.infrastructure.exchange.binance.exception.BinanceErrorCode;
import main.arbitrage.infrastructure.exchange.binance.exception.BinanceException;
import okhttp3.OkHttpClient;

public class BaseBinancePublicRestService implements ExchangeRestService {
  protected final OkHttpClient okHttpClient;
  protected final ObjectMapper objectMapper;
  protected final SymbolVariableService symbolVariableService;

  protected final String DEFAULT_URL = "https://fapi.binance.com/fapi";

  public BaseBinancePublicRestService(
      OkHttpClient okHttpClient,
      ObjectMapper objectMapper,
      SymbolVariableService symbolVariableService) {
    this.okHttpClient = okHttpClient;
    this.objectMapper = objectMapper;
    this.symbolVariableService = symbolVariableService;
  }

  @Override
  public String convertSymbol(String symbol) {
    String upperSymbol = symbol.toUpperCase().replace("USDT", "");

    if (!symbolVariableService.isSupportedSymbol(upperSymbol))
      throw new BinanceException(BinanceErrorCode.BAD_SYMBOL);

    return upperSymbol + "USDT";
  }

  @Override
  public String generateQueryHash(String queryString) {
    return null;
  }

  @Override
  public String generateQueryString(Map<String, Object> params) {
    return params.entrySet().stream()
        .map(entry -> entry.getKey() + "=" + entry.getValue())
        .collect(Collectors.joining("&"));
  }

  @Override
  public String generateToken() {
    return null;
  }

  @Override
  public String generateToken(Map<String, Object> params) {
    return null;
  }

  @Override
  public void validateResponse(JsonNode json) {
    String errorCode = json.get("code").asText();
    switch (errorCode) {
      // General Server or Network issues (10xx)
      case "-1000":
        throw new BinanceException(BinanceErrorCode.UNKNOWN);
      case "-1001":
        throw new BinanceException(BinanceErrorCode.DISCONNECTED);
      case "-1002":
        throw new BinanceException(BinanceErrorCode.UNAUTHORIZED);
      case "-1008":
        throw new BinanceException(BinanceErrorCode.TOO_MANY_REQUESTS);
      case "-1021":
        throw new BinanceException(BinanceErrorCode.INVALID_TIMESTAMP);
      case "-1022":
        throw new BinanceException(BinanceErrorCode.INVALID_SIGNATURE);

      // Request issues (11xx - 2xxx)
      case "-1102":
        throw new BinanceException(BinanceErrorCode.MANDATORY_PARAM_EMPTY_OR_MALFORMED);
      case "-1121":
        throw new BinanceException(BinanceErrorCode.BAD_SYMBOL);
      case "-2010":
        throw new BinanceException(BinanceErrorCode.NEW_ORDER_REJECTED);
      case "-2013":
        throw new BinanceException(BinanceErrorCode.NO_SUCH_ORDER);
      case "-2014":
        throw new BinanceException(BinanceErrorCode.BAD_API_KEY_FMT);
      case "-2015":
        throw new BinanceException(BinanceErrorCode.INVALID_API_KEY_IP_PERMISSION);
      case "-2018":
        throw new BinanceException(BinanceErrorCode.BALANCE_NOT_SUFFICIENT);

      // Filters and other issues (3xxx-5xxx)
      case "-4001":
        throw new BinanceException(BinanceErrorCode.PRICE_LESS_THAN_ZERO);
      case "-4003":
        throw new BinanceException(BinanceErrorCode.QTY_LESS_THAN_ZERO);
      case "-4055":
        throw new BinanceException(BinanceErrorCode.AMOUNT_MUST_BE_POSITIVE);
      default:
        throw new BinanceException(BinanceErrorCode.UNKNOWN);
    }
  }
}
