package main.arbitrage.infrastructure.exchange.binance.priv.rest.dto.order;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BinancePostOrderRequestDto {
    private String newClientOrderId;
    private Type type;
    private String symbol;
    private Side side;
    private String newOrderRespType;
    private Double quantity;
    private Double price;
    private String timeInForce;
    private Long timestamp;
    // private Long goodTillDate;

    @Builder
    public BinancePostOrderRequestDto(
        String newClientOrderId,
        Type type,
        String symbol,
        Side side,
        Double quantity,
        Double price
    ) {
        this.newClientOrderId = newClientOrderId;
        this.type = type;
        this.symbol = symbol;
        this.side = side;
        this.quantity = quantity;
        this.price = price;
        // GTD로 변경시 자동 취소가 가능하지만 최소 10분동안 Order를 유지해야함 -> 배제이유
        this.timeInForce = type.equals(Type.LIMIT) ? TimeInForce.GTC.name() : null;
        // this.goodTillDate = System.currentTimeMillis() + 1000 * 601;
        this.newOrderRespType = "RESULT";
        this.timestamp = System.currentTimeMillis();
    }
}