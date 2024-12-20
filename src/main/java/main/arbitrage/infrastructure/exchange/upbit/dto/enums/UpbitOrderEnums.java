package main.arbitrage.infrastructure.exchange.upbit.dto.enums;

public class UpbitOrderEnums {
    public enum OrdType {
        limit, market, price
    }
    public enum Side {
        bid, ask
    }
    public enum State {
        wait, watch, done, cancel
    }
}
