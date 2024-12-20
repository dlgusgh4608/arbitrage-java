package main.arbitrage.infrastructure.exchange.binance.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import main.arbitrage.infrastructure.exchange.binance.dto.enums.BinanceEnums.Side;
import main.arbitrage.infrastructure.exchange.binance.dto.enums.BinanceEnums.Status;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
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


    @Builder
    public BinanceOrderResponse(String clientOrderId, String symbol, Side side, Status status,
            String price, String origQty, String avgPrice, String executedQty, String cumQuote) {
        this.clientOrderId = clientOrderId;
        this.symbol = symbol;
        this.side = side;
        this.status = status;
        this.price = Double.parseDouble(price);
        this.origQty = Double.parseDouble(origQty);
        this.avgPrice = Double.parseDouble(avgPrice);
        this.executedQty = Double.parseDouble(executedQty);
        this.cumQuote = Double.parseDouble(cumQuote);
    }
}
