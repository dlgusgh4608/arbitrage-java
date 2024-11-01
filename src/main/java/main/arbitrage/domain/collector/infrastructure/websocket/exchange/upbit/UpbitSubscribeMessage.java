package main.arbitrage.domain.collector.infrastructure.websocket.exchange.upbit;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UpbitSubscribeMessage {
    private String ticket;
    private String type;
    private String[] codes;

    public static List<UpbitSubscribeMessage> createSubscribeMessage(String uniqueTicket, String[] symbols) {
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
        String[] orderbookSymbols = Arrays.stream(symbols).map(symbol -> symbol.concat(".10")).toArray(String[]::new);

        // orderbook 타입 메시지 추가
        messages.add(UpbitSubscribeMessage.builder()
                .type("orderbook")
                .codes(formatSymbols(orderbookSymbols))
                .build());

        System.out.println(messages);
        return messages;
    }

    private static String[] formatSymbols(String[] symbols) {
        return Arrays.stream(symbols)
                .map(symbol -> "KRW-" + symbol.toUpperCase())
                .toArray(String[]::new);
    }
}