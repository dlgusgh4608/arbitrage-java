package main.arbitrage.infrastructure.upbit.websocket;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.ArrayList;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@Slf4j
@ToString
public class UpbitSubscribeMessage {
  private String ticket;
  private String type;
  private String[] codes;

  public static List<UpbitSubscribeMessage> createSubscribeMessage(
      String uniqueTicket, List<String> symbols) {
    List<UpbitSubscribeMessage> messages = new ArrayList<>();

    // ticket 메시지 추가
    messages.add(UpbitSubscribeMessage.builder().ticket(uniqueTicket).build());

    // trade 타입 메시지 추가
    messages.add(
        UpbitSubscribeMessage.builder().type("trade").codes(formatSymbols(symbols)).build());

    // orderbook depth는 Binance기준 10개로 맞춤. (detph10이면 충분. Front-end UI용도.)
    List<String> orderbookSymbols = symbols.stream().map(symbol -> symbol.concat(".10")).toList();

    // orderbook 타입 메시지 추가
    messages.add(
        UpbitSubscribeMessage.builder()
            .type("orderbook")
            .codes(formatSymbols(orderbookSymbols))
            .build());

    log.info("upbit subscribe message: {}", messages);

    return messages;
  }

  private static String[] formatSymbols(List<String> symbols) {
    return symbols.stream().map(symbol -> "KRW-" + symbol.toUpperCase()).toArray(String[]::new);
  }
}
