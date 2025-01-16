package main.arbitrage.application.auto.service;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AutomaticOrder {
  private final String email;
  private final ExecutorService executorService = Executors.newSingleThreadScheduledExecutor();

  public AutomaticOrder(String email) {
    this.email = email;
  }

  public void run() {
    executorService.execute(
        () -> {
          // 자동거래 로직.
        });
  }
}
