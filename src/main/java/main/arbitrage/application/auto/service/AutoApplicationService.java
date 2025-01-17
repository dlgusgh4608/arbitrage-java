// package main.arbitrage.application.auto.service;

// import jakarta.annotation.PostConstruct;
// import java.util.ArrayList;
// import java.util.HashMap;
// import java.util.List;
// import java.util.Map;
// import java.util.Optional;
// import lombok.RequiredArgsConstructor;
// import main.arbitrage.application.collector.dto.PremiumDTO;
// import main.arbitrage.domain.buyOrder.service.BuyOrderService;
// import main.arbitrage.domain.exchangeRate.service.ExchangeRateService;
// import main.arbitrage.domain.sellOrder.service.SellOrderService;
// import main.arbitrage.domain.symbol.entity.Symbol;
// import main.arbitrage.domain.symbol.service.SymbolVariableService;
// import main.arbitrage.domain.user.entity.User;
// import main.arbitrage.domain.user.service.UserService;
// import main.arbitrage.domain.userEnv.entity.UserEnv;
// import main.arbitrage.domain.userEnv.service.UserEnvService;
// import main.arbitrage.infrastructure.exchange.binance.priv.websocket.BinanceUserStream;
// import main.arbitrage.infrastructure.exchange.dto.ExchangePrivateRestPair;
// import main.arbitrage.infrastructure.exchange.factory.ExchangePrivateRestFactory;
// import main.arbitrage.infrastructure.websocket.handler.BinanceUserStreamHandler;
// import org.springframework.context.event.EventListener;
// import org.springframework.stereotype.Service;

// @Service
// @RequiredArgsConstructor
// public class AutoApplicationService {
//   private final ExchangePrivateRestFactory exchangePrivateRestFactory;
//   private final SymbolVariableService symbolVariableService;
//   private final BinanceUserStreamHandler binanceUserStreamHandler;
//   private final UserService userService;
//   private final UserEnvService userEnvService;
//   private final ExchangeRateService exchangeRateService;
//   private final BuyOrderService buyOrderService;
//   private final SellOrderService sellOrderService;
//   private final Map<Long, BinanceUserStream> userStreams = new HashMap<>();
//   private final List<AutomaticOrder> autoMaticOrders = new ArrayList<>();

//   @EventListener
//   public void helloworld(PremiumDTO dto) {
//     for (AutomaticOrder autoMaticOrder : autoMaticOrders) {
//       autoMaticOrder.run();
//     }
//   }

//   @PostConstruct
//   public void init() {
//     Symbol symbol = symbolVariableService.findSymbolByName("eth");
//     List<User> users = userService.findAll();

//     for (User user : users) {
//       autoMaticOrders.add(new AutomaticOrder(user.getEmail()));

//       Optional<UserEnv> userEnv = userEnvService.findByUserId(user.getId());

//       if (userEnv.isPresent()) {
//         ExchangePrivateRestPair exchangePrivateServicePair =
//             exchangePrivateRestFactory.create(userEnv.get());
//         userStreams.put(
//             user.getId(),
//             new BinanceUserStream(
//                 user.getId(),
//                 symbol,
//                 binanceUserStreamHandler,
//                 exchangePrivateServicePair,
//                 buyOrderService,
//                 sellOrderService,
//                 exchangeRateService));

//         userStreams.get(user.getId()).connect();
//       }
//     }
//   }
// }
