package main.arbitrage.infrastructure.websocket.upbit;

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

        // orderbook 타입 메시지 추가 -> 나중에 Front 붙히면 주석 없애기
//        messages.add(UpbitSubscribeMessage.builder()
//                .type("orderbook")
//                .codes(formatSymbols(symbols))
//                .build());

        return messages;
    }

    private static String[] formatSymbols(String[] symbols) {
        return Arrays.stream(symbols)
                .map(symbol -> "KRW-" + symbol.toUpperCase())
                .toArray(String[]::new);
    }
}