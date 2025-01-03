package main.arbitrage.infrastructure.exchange.binance.pub.rest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import java.util.stream.Collectors;
import main.arbitrage.domain.symbol.service.SymbolVariableService;
import main.arbitrage.infrastructure.exchange.ExchangeRestService;
import main.arbitrage.infrastructure.exchange.binance.exception.BinanceRestException;
import okhttp3.OkHttpClient;

public abstract class BaseBinancePublicRestService implements ExchangeRestService {
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

    if (!symbolVariableService.getSupportedSymbolNames().contains(upperSymbol))
      throw new BinanceRestException("(바이낸스) 지원하지 않는 심볼입니다.", "BAD_SYMBOL");

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
  public void validateResponse(String responseBody)
      throws BinanceRestException, JsonProcessingException {
    JsonNode json = objectMapper.readTree(responseBody);
    String errorCode = json.get("code").asText();
    switch (errorCode) {
      // General Server or Network issues (10xx)
      case "-1000":
        throw new BinanceRestException("(바이낸스) 알 수 없는 에러가 발생했습니다.", "UNKNOWN");
      case "-1001":
        throw new BinanceRestException("(바이낸스) 내부 오류가 발생했습니다. 다시 시도해주세요.", "DISCONNECTED");
      case "-1002":
        throw new BinanceRestException("(바이낸스) 이 요청을 실행할 권한이 없습니다.", "UNAUTHORIZED");
      case "-1008":
        throw new BinanceRestException("(바이낸스) 너무 많은 요청이 대기 중입니다.", "TOO_MANY_REQUESTS");
      case "-1021":
        throw new BinanceRestException("(바이낸스) 타임스탬프가 recvWindow를 벗어났습니다.", "INVALID_TIMESTAMP");
      case "-1022":
        throw new BinanceRestException("(바이낸스) 이 요청에 대한 서명이 유효하지 않습니다.", "INVALID_SIGNATURE");

      // Request issues (11xx - 2xxx)
      case "-1102":
        throw new BinanceRestException(
            "(바이낸스) 필수 파라미터가 전송되지 않았거나, 비어있거나, 잘못되었습니다.", "MANDATORY_PARAM_EMPTY_OR_MALFORMED");
      case "-1121":
        throw new BinanceRestException("(바이낸스) 잘못된 심볼입니다.", "BAD_SYMBOL");
      case "-2010":
        throw new BinanceRestException("(바이낸스) 새로운 주문이 거부되었습니다.", "NEW_ORDER_REJECTED");
      case "-2013":
        throw new BinanceRestException("(바이낸스) 주문이 존재하지 않습니다.", "NO_SUCH_ORDER");
      case "-2014":
        throw new BinanceRestException("(바이낸스) 유효하지 않은 API 키 입니다.", "BAD_API_KEY_FMT");
      case "-2015":
        throw new BinanceRestException(
            "(바이낸스) API 키, IP 또는 권한이 유효하지 않습니다.", "INVALID_API_KEY_IP_PERMISSION");
      case "-2018":
        throw new BinanceRestException("(바이낸스) 잔액이 부족합니다.", "BALANCE_NOT_SUFFICIENT");

      // Filters and other issues (3xxx-5xxx)
      case "-4001":
        throw new BinanceRestException("(바이낸스) 가격이 0보다 작습니다.", "PRICE_LESS_THAN_ZERO");
      case "-4003":
        throw new BinanceRestException("(바이낸스) 수량이 0보다 작습니다.", "QTY_LESS_THAN_ZERO");
      case "-4055":
        throw new BinanceRestException("(바이낸스) 금액은 양수여야 합니다.", "AMOUNT_MUST_BE_POSITIVE");
      default:
        throw new BinanceRestException("(바이낸스) 알 수 없는 에러가 발생했습니다.", "UNKNOWN_ERROR");
    }
  }
}
