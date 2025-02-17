package main.arbitrage.infrastructure.binance;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.List;
import java.util.Map;
import main.arbitrage.infrastructure.binance.dto.response.BinanceAccountResponse;
import main.arbitrage.infrastructure.binance.dto.response.BinanceChangeLeverageResponse;
import main.arbitrage.infrastructure.binance.dto.response.BinanceLeverageBracketResponse;
import main.arbitrage.infrastructure.binance.dto.response.BinanceOrderResponse;
import main.arbitrage.infrastructure.binance.dto.response.BinancePositionInfoResponse;
import main.arbitrage.infrastructure.binance.dto.response.BinanceSymbolInfoResponse;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.DeleteExchange;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;
import org.springframework.web.service.annotation.PutExchange;

@HttpExchange(contentType = "application/json", accept = "application/json")
public interface BinanceClient {

  // private
  @GetExchange("/v3/balance")
  List<BinanceAccountResponse> getAccounts(
      @RequestHeader("X-MBX-APIKEY") String accessKey,
      @RequestParam(name = "params") Map<String, Object> params);

  @PostExchange("/v1/order")
  BinanceOrderResponse createOrder(
      @RequestHeader("X-MBX-APIKEY") String accessKey,
      @RequestParam(name = "params") Map<String, Object> params);

  @DeleteExchange("/v1/order")
  BinanceOrderResponse cancelOrder(
      @RequestHeader("X-MBX-APIKEY") String accessKey,
      @RequestParam(name = "params") Map<String, Object> params);

  @GetExchange("/v1/symbolConfig")
  List<BinanceSymbolInfoResponse> getSymbolInfo(
      @RequestHeader("X-MBX-APIKEY") String accessKey,
      @RequestParam(name = "params") Map<String, Object> params);

  @GetExchange("/v1/leverageBracket")
  List<BinanceLeverageBracketResponse> getLeverageBrackets(
      @RequestHeader("X-MBX-APIKEY") String accessKey,
      @RequestParam(name = "params") Map<String, Object> params);

  @PostExchange("/v1/leverage")
  BinanceChangeLeverageResponse updateLeverage(
      @RequestHeader("X-MBX-APIKEY") String accessKey,
      @RequestParam(name = "params") Map<String, Object> params);

  @GetExchange("/v3/positionRisk")
  List<BinancePositionInfoResponse> getPositionInfo(
      @RequestHeader("X-MBX-APIKEY") String accessKey,
      @RequestParam(name = "params") Map<String, Object> params);

  @PostExchange("/v1/marginType")
  JsonNode updateMarginType(
      @RequestHeader("X-MBX-APIKEY") String accessKey,
      @RequestParam(name = "params") Map<String, Object> params);

  @PostExchange("/v1/listenKey")
  JsonNode createListenKey(
      @RequestHeader("X-MBX-APIKEY") String accessKey,
      @RequestParam(name = "params") Map<String, Object> params);

  @PutExchange("/v1/listenKey")
  JsonNode updateListenKey(
      @RequestHeader("X-MBX-APIKEY") String accessKey,
      @RequestParam(name = "params") Map<String, Object> params);

  // public
  @GetExchange("/v1/exchangeInfo")
  JsonNode getExchangeInfo();
}
