package main.arbitrage.infrastructure.exchange.binance.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import main.arbitrage.infrastructure.exchange.binance.dto.enums.BinanceEnums.Side;
import main.arbitrage.infrastructure.exchange.binance.dto.enums.BinanceEnums.Status;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@ToString
public class BinanceOrderResponse {
    private String clientOrderId; // 내가 만든 UUID

    private String symbol;
    private Side side;
    private Status status;

    private double price; // LIMIT일 경우 지정가
    private double origQty; // 원래 주문한 수량

    private double avgPrice; // 체결된 평단가
    private double executedQty; // 체결 수량
    private double cumQuote; // 사용한 돈
}
