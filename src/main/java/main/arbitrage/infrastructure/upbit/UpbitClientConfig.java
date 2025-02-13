package main.arbitrage.infrastructure.upbit;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

@Configuration
@RequiredArgsConstructor
public class UpbitClientConfig {
  private final UpbitErrorHandler upbitErrorHandler;

  @Bean
  public UpbitHttpInterface upbitHttpInterface() {
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
    return HttpServiceProxyFactory.builderFor(adapter)
        .build()
        .createClient(UpbitHttpInterface.class);
  }
}

// .requestInterceptor(
            //     (request, body, execution) -> {
            //       upbitErrorHandler.setRequest(request);
            //       return execution.execute(request, body);
            //     })
