package main.arbitrage.infrastructure.exchange.binance.dto.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import main.arbitrage.infrastructure.exchange.binance.dto.enums.BinanceEnums.Side;
import main.arbitrage.infrastructure.exchange.binance.dto.enums.BinanceEnums.TimeInForce;
import main.arbitrage.infrastructure.exchange.binance.dto.enums.BinanceEnums.Type;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BinancePostOrderRequest {
    private String newClientOrderId;
    private Type type;
    private String symbol;
    private Side side;
    private String newOrderRespType;
    private Double quantity;
    private Double price;
    private String timeInForce;
    private Long timestamp;

    @Builder
    public BinancePostOrderRequest(String newClientOrderId, Type type, String symbol, Side side,
            Double quantity, Double price) {
        this.newClientOrderId = newClientOrderId;
        this.type = type;
        this.symbol = symbol;
        this.side = side;
        this.quantity = quantity;
        this.price = price;
        this.timeInForce = type.equals(Type.LIMIT) ? TimeInForce.GTC.name() : null;
        this.newOrderRespType = "RESULT"; // RESULT시 FILLED가 나왔을때 Return해줌 Market일때.
        this.timestamp = System.currentTimeMillis();
    }
}
