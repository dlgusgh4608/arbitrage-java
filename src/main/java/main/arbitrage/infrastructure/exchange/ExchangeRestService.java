package main.arbitrage.infrastructure.exchange;

import java.util.Map;

public interface ExchangeRestService {
  void validateResponse(String responseBody) throws Exception;

  String convertSymbol(String symbol);

  String generateToken();

  String generateToken(Map<String, Object> params);

  String generateQueryString(Map<String, Object> params);

  String generateQueryHash(String queryString) throws Exception;
}
