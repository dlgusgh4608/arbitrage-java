package main.arbitrage;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import main.arbitrage.infrastructure.event.EventEmitter;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import main.arbitrage.service.Collector;
import main.arbitrage.infrastructure.websocket.exchange.upbit.UpbitWebSocket;
import main.arbitrage.infrastructure.websocket.exchange.binance.BinanceWebSocket;
import main.arbitrage.service.UsdToKrw;

@SpringBootApplication
public class ArbitrageApplication {
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final EventEmitter emitter = new EventEmitter(); // data type only JsonNode.

    public static void main(String[] args) throws InterruptedException {

        SpringApplication.run(ArbitrageApplication.class, args);

        UpbitWebSocket upbitWebSocket = new UpbitWebSocket(objectMapper);
        BinanceWebSocket binanceWebSocket = new BinanceWebSocket(objectMapper);

        new Collector(upbitWebSocket, binanceWebSocket, emitter, objectMapper);
        new UsdToKrw(objectMapper, emitter);
    }
}
