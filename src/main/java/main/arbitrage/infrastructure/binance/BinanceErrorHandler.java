package main.arbitrage.infrastructure.binance;

import static java.util.Map.entry;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.URI;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import main.arbitrage.global.exception.common.BaseHttpErrorHandler;
import main.arbitrage.infrastructure.binance.exception.BinanceErrorCode;
import main.arbitrage.infrastructure.binance.exception.BinanceException;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class BinanceErrorHandler extends BaseHttpErrorHandler {
  private final ObjectMapper objectMapper;
  private final Map<String, BinanceErrorCode> errorMap =
      Map.ofEntries(
          entry("-1000", BinanceErrorCode.UNKNOWN),
          entry("-1001", BinanceErrorCode.DISCONNECTED),
          entry("-1002", BinanceErrorCode.UNAUTHORIZED),
          entry("-1008", BinanceErrorCode.TOO_MANY_REQUESTS),
          entry("-1021", BinanceErrorCode.INVALID_TIMESTAMP),
          entry("-1022", BinanceErrorCode.INVALID_SIGNATURE),
          entry("-1102", BinanceErrorCode.MANDATORY_PARAM_EMPTY_OR_MALFORMED),
          entry("-1121", BinanceErrorCode.BAD_SYMBOL),
          entry("-2010", BinanceErrorCode.NEW_ORDER_REJECTED),
          entry("-2013", BinanceErrorCode.NO_SUCH_ORDER),
          entry("-2014", BinanceErrorCode.BAD_API_KEY_FMT),
          entry("-2015", BinanceErrorCode.INVALID_API_KEY_IP_PERMISSION),
          entry("-2018", BinanceErrorCode.BALANCE_NOT_SUFFICIENT),
          entry("-4001", BinanceErrorCode.PRICE_LESS_THAN_ZERO),
          entry("-4003", BinanceErrorCode.QTY_LESS_THAN_ZERO),
          entry("-4055", BinanceErrorCode.AMOUNT_MUST_BE_POSITIVE),
          entry("0", BinanceErrorCode.INVALID_LOCATION));

  @Override
  public boolean hasError(ClientHttpResponse response) {
    try {
      return response.getStatusCode().isError();
    } catch (Exception e) {
      throw new BinanceException(BinanceErrorCode.UNKNOWN, e);
    }
  }

  @Override
  public void handleError(ClientHttpResponse response) {
    URI uri = requestContext.get().request().getURI();
    String body = requestContext.get().body();

    String errorMsg = String.format("\nurl: %s\nbody: %s", uri.toString(), body);
    try {
      JsonNode errorNode = objectMapper.readTree(response.getBody());

      System.out.println(errorNode);

      String key = errorNode.get("code").asText();

      BinanceErrorCode errorCode = errorMap.get(key);

      if (errorCode == null) throw new BinanceException(BinanceErrorCode.UNKNOWN, errorMsg);

      throw new BinanceException(errorCode, errorMsg);
    } catch (BinanceException e) {
      throw e;
    } catch (IOException e) {
      throw new BinanceException(BinanceErrorCode.API_ERROR, errorMsg, e);
    } finally {
      requestContext.remove();
    }
  }
}
