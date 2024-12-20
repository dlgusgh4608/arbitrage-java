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
    private BinanceOrderEnum.Type type;
    private String symbol;
    private BinanceOrderEnum.Side side;
    private String newOrderRespType;
    private Double quantity;
    private Double price;
    private String timeInForce;
    private Long timestamp;

    @Builder
    public BinancePostOrderRequestDto(String newClientOrderId, BinanceOrderEnum.Type type,
            String symbol, BinanceOrderEnum.Side side, Double quantity, Double price) {
        this.newClientOrderId = newClientOrderId;
        this.type = type;
        this.symbol = symbol;
        this.side = side;
        this.quantity = quantity;
        this.price = price;
        this.timeInForce =
                type.equals(BinanceOrderEnum.Type.LIMIT) ? BinanceOrderEnum.TimeInForce.GTC.name()
                        : null;
        this.newOrderRespType = "RESULT"; // RESULT시 FILLED가 나왔을때 Return해줌 Market일때.
        this.timestamp = System.currentTimeMillis();
    }
}
