package main.arbitrage.infrastructure.exchange;

import java.util.Map;

public interface ExchangePrivateRestService {
    void validateResponse(String responseBody) throws Exception;

    String generateToken();

    String generateToken(Map<String, Object> params);

    String generateQueryString(Map<String, Object> params);

    String generateQueryHash(String queryString) throws Exception;
}