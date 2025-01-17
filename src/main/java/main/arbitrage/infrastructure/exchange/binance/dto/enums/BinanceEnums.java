package main.arbitrage.infrastructure.exchange.binance.dto.enums;

public class BinanceEnums {
  public enum Type {
    LIMIT,
    MARKET
  }

  public enum TimeInForce {
    IOC,
    GTC,
    GTD
  }

  public enum Status {
    NEW,
    PARTIALLY_FILLED,
    FILLED,
    CANCELED,
    REJECTED
  }

  public enum Side {
    BUY,
    SELL
  }

  public enum MarginType {
    ISOLATED,
    CROSSED
  }

  public enum EventType {
    ACCOUNT_UPDATE,
    ORDER_TRADE_UPDATE,
    listenKeyExpired,
  }
}
