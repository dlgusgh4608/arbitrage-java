package main.arbitrage.infrastructure.exchange.binance.priv.rest.dto.order;

public class BinanceOrderEnum {
    public enum Type { LIMIT, MARKET }
    public enum TimeInForce { IOC, GTC, GTD }
    public enum Status { NEW, PARTIALLY_FILLED, FILLED, CANCELED, REJECTED }
    public enum Side { BUY, SELL }
}
