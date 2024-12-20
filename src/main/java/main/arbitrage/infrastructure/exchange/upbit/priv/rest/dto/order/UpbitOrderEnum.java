package main.arbitrage.infrastructure.exchange.upbit.priv.rest.dto.order;

public class UpbitOrderEnum {
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
