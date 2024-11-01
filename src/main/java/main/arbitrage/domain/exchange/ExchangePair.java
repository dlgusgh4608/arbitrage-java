package main.arbitrage.domain.exchange;

import lombok.Getter;
import main.arbitrage.infrastructure.websocket.common.dto.CommonTradeDto;

@Getter
public class ExchangePair {
    private final CommonTradeDto domestic;
    private final CommonTradeDto overseas;

    public ExchangePair(CommonTradeDto domestic, CommonTradeDto overseas) {
        this.domestic = domestic;
        this.overseas = overseas;
    }
}