package main.arbitrage.infrastructure.exchange.binance.dto.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;
import main.arbitrage.infrastructure.exchange.binance.dto.enums.BinanceEnums.Side;
import main.arbitrage.infrastructure.exchange.binance.dto.enums.BinanceEnums.TimeInForce;
import main.arbitrage.infrastructure.exchange.binance.dto.enums.BinanceEnums.Type;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BinancePostOrderRequest {
    private final String newClientOrderId;
    private final Type type;
    private final String symbol;
    private final Side side;
    private final String newOrderRespType;
    private final Double quantity;
    private final Double price;
    private final String timeInForce;
    private final Long timestamp;

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
