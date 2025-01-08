package main.arbitrage.application.exchangeRate.service;

import lombok.RequiredArgsConstructor;
import main.arbitrage.domain.exchangeRate.service.ExchangeRateService;
import main.arbitrage.infrastructure.crawler.UsdToKrwCrawler;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ExchangeRateApplicationService {
  private final UsdToKrwCrawler usdToKrwCrawler;
  private final ExchangeRateService exchangeRateService;

  @Scheduled(fixedDelay = 1000 * 10) // 10sec
  protected void scheduleOnEmit() {
    float usdToKrw = usdToKrwCrawler.craw();

    if (usdToKrw == 0) return;

    exchangeRateService.setExchangeRate(usdToKrw, "USD", "KRW");
  }
}
