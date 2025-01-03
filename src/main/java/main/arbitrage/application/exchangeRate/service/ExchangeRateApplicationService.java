package main.arbitrage.application.exchangeRate.service;

import lombok.RequiredArgsConstructor;
import main.arbitrage.application.exchangeRate.dto.ExchangeRateDTO;
import main.arbitrage.domain.exchangeRate.entity.ExchangeRate;
import main.arbitrage.domain.exchangeRate.service.ExchangeRateService;
import main.arbitrage.infrastructure.crawler.UsdToKrwCrawler;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ExchangeRateApplicationService {
  private final UsdToKrwCrawler usdToKrwCrawler;
  private final ExchangeRateService exchangeRateService;
  private final ApplicationEventPublisher publisher;

  // private final ObjectMapper objectMapper;
  // private final EventEmitter emitter;

  @Scheduled(fixedDelay = 1000 * 10) // 10sec
  protected void scheduleOnEmit() {
    double usdToKrw = usdToKrwCrawler.craw();

    ExchangeRateDTO exchangeRateDto =
        ExchangeRateDTO.builder().fromCurrency("USD").toCurrency("KRW").rate(usdToKrw).build();

    ExchangeRate exchangeRate = exchangeRateService.setExchangeRate(exchangeRateDto);

    if (exchangeRate == null) return;

    publisher.publishEvent(exchangeRate);
  }
}
