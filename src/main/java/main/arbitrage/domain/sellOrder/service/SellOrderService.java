package main.arbitrage.domain.sellOrder.service;

import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import main.arbitrage.domain.sellOrder.repository.SellOrderRepository;

@Service
@RequiredArgsConstructor
public class SellOrderService {
        private final double BINANCE_TAKER_COMM = 0.0005d;
        private final SellOrderRepository sellOrderRepository;

}
