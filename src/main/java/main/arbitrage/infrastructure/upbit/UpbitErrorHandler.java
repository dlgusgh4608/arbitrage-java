package main.arbitrage.infrastructure.upbit;

import static java.util.Map.entry;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import main.arbitrage.infrastructure.exchange.upbit.exception.UpbitErrorCode;
import main.arbitrage.infrastructure.exchange.upbit.exception.UpbitException;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Controller;
import org.springframework.web.client.ResponseErrorHandler;

@Controller
@RequiredArgsConstructor
public class UpbitErrorHandler implements ResponseErrorHandler {
  private final ObjectMapper objectMapper;
  private final Map<String, UpbitErrorCode> errorMap =
      Map.ofEntries(
          entry("invalid_query_payload", UpbitErrorCode.INVALID_QUERY_PAYLOAD),
          entry("jwt_verification", UpbitErrorCode.JWT_VERIFICATION),
          entry("expired_access_key", UpbitErrorCode.EXPIRED_ACCESS_KEY),
          entry("nonce_used", UpbitErrorCode.NONCE_USED),
          entry("no_authorization_ip", UpbitErrorCode.NO_AUTHORIZATION_IP),
          entry("out_of_scope", UpbitErrorCode.OUT_OF_SCOPE),
          entry("create_ask_error", UpbitErrorCode.CREATE_ASK_ERROR),
          entry("create_bid_error", UpbitErrorCode.CREATE_BID_ERROR),
          entry("insufficient_funds_ask", UpbitErrorCode.INSUFFICIENT_FUNDS_ASK),
          entry("insufficient_funds_bid", UpbitErrorCode.INSUFFICIENT_FUNDS_BID),
          entry("under_min_total_ask", UpbitErrorCode.UNDER_MIN_TOTAL_ASK),
          entry("under_min_total_bid", UpbitErrorCode.UNDER_MIN_TOTAL_BID),
          entry("withdraw_address_not_registerd", UpbitErrorCode.BAD_REQUEST),
          entry("validation_error", UpbitErrorCode.INVALID_PARAMETER),
          entry("invalid_parameter", UpbitErrorCode.INVALID_PARAMETER));

  private final ThreadLocal<RequestContext> requestContext = new ThreadLocal<>();

  private static record RequestContext(HttpRequest request, String body) {}

  public void setRequest(HttpRequest request, byte[] body) {
    requestContext.set(new RequestContext(request, new String(body, StandardCharsets.UTF_8)));
  }

  // API호출시 에러를 catch함.
  @Override
  public boolean hasError(ClientHttpResponse response) {
    try {
      return response.getStatusCode().isError();
    } catch (Exception e) {
      throw new UpbitException(UpbitErrorCode.UNKNOWN, e);
    }
  }

  @Override
  public void handleError(ClientHttpResponse response) {
    URI uri = requestContext.get().request().getURI();
    String body = requestContext.get().body();

    String errorMsg = String.format("\nurl: %s\nbody: %s", uri.toString(), body);
    try {

      JsonNode errorNode = objectMapper.readTree(response.getBody());

      String key = errorNode.get("error").get("name").asText();

      UpbitErrorCode errorCode = errorMap.get(key);

      if (errorCode == null) {
        throw new UpbitException(UpbitErrorCode.UNKNOWN, errorMsg);
      }

      throw new UpbitException(errorCode, errorMsg);
    } catch (UpbitException e) {
      throw e;
    } catch (IOException e) {
      throw new UpbitException(UpbitErrorCode.API_ERROR, errorMsg, e);
    } finally {
      requestContext.remove();
    }
  }
}
