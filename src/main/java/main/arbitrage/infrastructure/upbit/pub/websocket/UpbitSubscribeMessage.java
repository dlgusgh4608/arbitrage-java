package main.arbitrage.infrastructure.upbit.pub.websocket;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@Slf4j
public class UpbitSubscribeMessage {
    private String ticket;
    private String type;
    private String[] codes;

    public static List<UpbitSubscribeMessage> createSubscribeMessage(String uniqueTicket, List<String> symbols) {
        List<UpbitSubscribeMessage> messages = new ArrayList<>();

        // ticket 메시지 추가
        messages.add(UpbitSubscribeMessage.builder()
                .ticket(uniqueTicket)
                .build());

        // trade 타입 메시지 추가
        messages.add(UpbitSubscribeMessage.builder()
                .type("trade")
                .codes(formatSymbols(symbols))
                .build());

        // orderbook depth는 Binance기준 10개로 맞춤. (detph10이면 충분. Front-end UI용도.)
        List<String> orderbookSymbols = symbols.stream().map(symbol -> symbol.concat(".10")).toList();

        // orderbook 타입 메시지 추가
        messages.add(UpbitSubscribeMessage.builder()
                .type("orderbook")
                .codes(formatSymbols(orderbookSymbols))
                .build());

        log.info("subscribe message: {}", messages);
        return messages;
    }

    private static String[] formatSymbols(List<String> symbols) {
        return symbols.stream()
                .map(symbol -> "KRW-" + symbol.toUpperCase())
                .toArray(String[]::new);
    }
}