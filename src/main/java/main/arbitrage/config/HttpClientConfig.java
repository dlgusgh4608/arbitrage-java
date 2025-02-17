package main.arbitrage.config;

import lombok.RequiredArgsConstructor;
import main.arbitrage.infrastructure.binance.BinanceClient;
import main.arbitrage.infrastructure.binance.BinanceErrorHandler;
import main.arbitrage.infrastructure.upbit.UpbitClient;
import main.arbitrage.infrastructure.upbit.UpbitErrorHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

@Configuration
@RequiredArgsConstructor
public class HttpClientConfig {
  private final UpbitErrorHandler upbitErrorHandler;
  private final BinanceErrorHandler binanceErrorHandler;

  @Bean
  public UpbitClient upbitClient() {
    RestClient resClient =
        RestClient.builder()
            .baseUrl("https://api.upbit.com")
            .defaultHeader("Content-Type", "application/json")
            .defaultStatusHandler(upbitErrorHandler)
            .requestInterceptor(
                (request, body, execution) -> {
                  upbitErrorHandler.setRequest(request, body);
                  return execution.execute(request, body);
                })
            .build();

    RestClientAdapter adapter = RestClientAdapter.create(resClient);
    return HttpServiceProxyFactory.builderFor(adapter).build().createClient(UpbitClient.class);
  }

  @Bean
  public BinanceClient binanceClient() {
    RestClient resClient =
        RestClient.builder()
            .baseUrl("https://fapi.binance.com/fapi")
            .defaultHeader("Content-Type", "application/json")
            .defaultStatusHandler(binanceErrorHandler)
            .requestInterceptor(
                (request, body, execution) -> {
                  binanceErrorHandler.setRequest(request, body);
                  return execution.execute(request, body);
                })
            .build();

    RestClientAdapter adapter = RestClientAdapter.create(resClient);
    return HttpServiceProxyFactory.builderFor(adapter).build().createClient(BinanceClient.class);
  }
}
