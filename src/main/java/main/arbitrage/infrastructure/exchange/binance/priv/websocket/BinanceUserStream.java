// package main.arbitrage.infrastructure.exchange.binance.priv.websocket;

// import com.fasterxml.jackson.databind.JsonNode;
// import com.fasterxml.jackson.databind.ObjectMapper;
// import lombok.RequiredArgsConstructor;
// import lombok.extern.slf4j.Slf4j;
// import main.arbitrage.infrastructure.exchange.dto.ExchangePrivateRestPair;
// import main.arbitrage.infrastructure.websocket.client.BaseWebSocketClient;
// import org.springframework.web.socket.WebSocketSession;

// @RequiredArgsConstructor
// @Slf4j
// public class BinanceUserStream extends BaseWebSocketClient {
//   private static final String BASE_URL = "wss://fstream.binance.com/ws/";
//   private final ExchangePrivateRestPair exchangePrivateRestPair;
//   private final ObjectMapper objectMapper;

//   private boolean isRunning = false;
//   private WebSocketSession session;

//   @Override
//   public void connect() {
//     // try {
//     //   if (isRunning) throw new IllegalStateException("Binance WebSocket is already running!");
//     //   StandardWebSocketClient client = new StandardWebSocketClient();

//     //   String listenKey = exchangePrivateRestPair.getBinance().createListenKey();

//     //   String websocketURL = BASE_URL.concat(listenKey);
//     //   ExchangeWebSocketHandler handler =
//     //       new ExchangeWebSocketHandler("BinanceStream", this::handleMessage, objectMapper);
//     //   session = client.execute(handler, websocketURL).get();
//     //   isRunning = true;
//     // } catch (Exception e) {
//     //   System.out.println(e);
//     //   throw new RuntimeException(e);
//     // }
//   }

//   @Override
//   public void disconnect() {}

//   @Override
//   public boolean isConnected() {
//     return false;
//   }

//   @Override
//   protected void handleMessage(JsonNode message) {}

//   @Override
//   protected void handleTrade(JsonNode data) {}

//   @Override
//   protected void handleOrderbook(JsonNode data) {}
// }
