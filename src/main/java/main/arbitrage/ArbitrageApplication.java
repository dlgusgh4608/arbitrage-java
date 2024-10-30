package main.arbitrage;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import main.arbitrage.service.Collector;
import main.arbitrage.infrastructure.websocket.exchange.upbit.UpbitWebSocket;
import main.arbitrage.infrastructure.websocket.exchange.binance.BinanceWebSocket;


@EnableScheduling
@SpringBootApplication
public class ArbitrageApplication {

    public static void main(String[] args) {

        SpringApplication.run(ArbitrageApplication.class, args);

        ObjectMapper objectMapper = new ObjectMapper();

        UpbitWebSocket upbitWebSocket = new UpbitWebSocket(objectMapper);
        BinanceWebSocket binanceWebSocket = new BinanceWebSocket(objectMapper);
        Collector collector = new Collector(upbitWebSocket, binanceWebSocket);
        collector.run();
    }
}
