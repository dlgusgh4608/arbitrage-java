package main.arbitrage.infrastructure.exchange;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.Map;

public interface ExchangeRestService {
  void validateResponse(JsonNode json);

  String convertSymbol(String symbol);

  String generateToken();

  String generateToken(Map<String, Object> params);

  String generateQueryString(Map<String, Object> params);

  String generateQueryHash(String queryString) throws Exception;
}
