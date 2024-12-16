package main.arbitrage.domain.buyOrder.service;

import lombok.RequiredArgsConstructor;
import main.arbitrage.domain.buyOrder.entity.BuyOrder;
import main.arbitrage.domain.buyOrder.repository.BuyOrderRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BuyOrderService {
    private final BuyOrderRepository buyOrderRepository;


    public BuyOrder create(BuyOrder buyOrder) {
        return buyOrderRepository.save(buyOrder);
    }
}