package main.arbitrage.infrastructure.upbit;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.List;
import java.util.Map;
import main.arbitrage.infrastructure.upbit.dto.response.UpbitAccountResponse;
import main.arbitrage.infrastructure.upbit.dto.response.UpbitOrderResponse;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

@HttpExchange(url = "/v1", contentType = "application/json", accept = "application/json")
public interface UpbitHttpInterface {

  @GetExchange("/accounts")
  List<UpbitAccountResponse> getAccounts(@RequestHeader("Authorization") String authHeader);

  @PostExchange("/orders")
  JsonNode createOrder(
      @RequestHeader("Authorization") String authHeader, @RequestBody Map<String, Object> body);

  @GetExchange("/order")
  UpbitOrderResponse getOrder(
      @RequestHeader("Authorization") String authHeader,
      @RequestParam(name = "uuid", required = true) String uuid);
}
