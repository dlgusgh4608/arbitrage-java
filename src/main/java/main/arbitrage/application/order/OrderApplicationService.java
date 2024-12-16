package main.arbitrage.application.order;

import lombok.RequiredArgsConstructor;
import main.arbitrage.domain.buyOrder.service.BuyOrderService;
import main.arbitrage.infrastructure.exchange.factory.ExchangePrivateRestFactory;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrderApplicationService {
    private final BuyOrderService buyOrderService;
    private final ExchangePrivateRestFactory exchangePrivateRestFactory;
    
}