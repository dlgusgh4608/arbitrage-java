package main.arbitrage.application.auto.service;

import jakarta.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import main.arbitrage.application.auto.dto.AutomaticUserInfoDTO;
import main.arbitrage.application.collector.dto.PremiumDTO;
import main.arbitrage.auth.security.SecurityUtil;
import main.arbitrage.domain.autoTradingStrategy.entity.AutoTradingStrategy;
import main.arbitrage.domain.autoTradingStrategy.service.AutoTradingStrategyService;
import main.arbitrage.domain.buyOrder.service.BuyOrderService;
import main.arbitrage.domain.exchangeRate.service.ExchangeRateService;
import main.arbitrage.domain.price.service.PriceService;
import main.arbitrage.domain.sellOrder.service.SellOrderService;
import main.arbitrage.domain.symbol.entity.Symbol;
import main.arbitrage.domain.symbol.service.SymbolVariableService;
import main.arbitrage.domain.user.entity.User;
import main.arbitrage.domain.user.service.UserService;
import main.arbitrage.domain.userEnv.service.UserEnvService;
import main.arbitrage.global.util.aes.AESCrypto;
import main.arbitrage.infrastructure.exchange.binance.dto.response.BinanceExchangeInfoResponse;
import main.arbitrage.infrastructure.exchange.binance.priv.websocket.BinanceUserStream;
import main.arbitrage.infrastructure.exchange.binance.pub.rest.BinancePublicRestService;
import main.arbitrage.infrastructure.exchange.dto.ExchangePrivateRestPair;
import main.arbitrage.infrastructure.exchange.factory.ExchangePrivateRestFactory;
import main.arbitrage.infrastructure.websocket.handler.BinanceUserStreamHandler;
import main.arbitrage.presentation.dto.form.AutoTradingStrategyForm;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AutoApplicationService {
  private final AutoTradingStrategyService autoTradingStrategyService;
  private final AESCrypto aesCrypto;
  private final ExchangePrivateRestFactory exchangePrivateRestFactory;
  private final SymbolVariableService symbolVariableService;
  private final BinanceUserStreamHandler binanceUserStreamHandler;
  private final UserService userService;
  private final UserEnvService userEnvService;
  private final ExchangeRateService exchangeRateService;
  private final BuyOrderService buyOrderService;
  private final SellOrderService sellOrderService;
  private final PriceService priceService;
  private final BinancePublicRestService binancePublicRestService;
  private final Map<Long, BinanceUserStream> userStreams = new HashMap<>();
  private Map<String, BinanceExchangeInfoResponse> binanceExchangeInfoMap = new HashMap<>();

  @EventListener
  public void premiumConsumer(PremiumDTO dto) {
    userStreams.values().forEach(v -> v.run(dto));
  }

  @EventListener
  public void exchangeInfoConsumer(
      Map<String, BinanceExchangeInfoResponse> binanceExchangeInfoMap) {
    this.binanceExchangeInfoMap = binanceExchangeInfoMap;
  }

  /*
   * 자동거래의 Thread를 생성하는 로직
   * 1. UserStream(청산 방지 및 자동거래)
   * 2. PremiumDTO의 event를 broadcasting하여 자동거래를 진행
   */
  @PostConstruct
  public void init() {
    binanceExchangeInfoMap = binancePublicRestService.getExchangeInfo();
    List<AutomaticUserInfoDTO> automaticUsers = userEnvService.findAutomaticUsers();
    for (AutomaticUserInfoDTO automaticUser : automaticUsers) {
      ExchangePrivateRestPair exchangePrivateServicePair =
          exchangePrivateRestFactory.create(
              aesCrypto.decrypt(automaticUser.upbitAccessKey()),
              aesCrypto.decrypt(automaticUser.upbitSecretKey()),
              aesCrypto.decrypt(automaticUser.binanceAccessKey()),
              aesCrypto.decrypt(automaticUser.binanceSecretKey()));

      String symbolName = automaticUser.autoTradingStrategy().getSymbol().getName();

      BinanceUserStream userStream =
          new BinanceUserStream(
              automaticUser,
              binanceExchangeInfoMap.get(symbolName),
              binanceUserStreamHandler,
              exchangePrivateServicePair,
              symbolVariableService,
              buyOrderService,
              sellOrderService,
              exchangeRateService,
              priceService);

      userStreams.put(automaticUser.userId(), userStream);

      userStream.connect();
    }
  }

  @Transactional
  public AutoTradingStrategyForm getAutoTradingStrategyForm() {
    Long userId = SecurityUtil.getUserId();
    User user = userService.findAndExistByUserId(userId);
    Optional<AutoTradingStrategy> autoTradingStrategy =
        autoTradingStrategyService.findByUserId(userId);

    if (autoTradingStrategy.isPresent()) {
      return AutoTradingStrategyForm.fromEntity(user, autoTradingStrategy.get());
    }

    return null;
  }

  @Transactional
  public AutoTradingStrategyForm updateAutoTradingStrategy(
      AutoTradingStrategyForm autoTradingStrategyForm) {
    Long userId = SecurityUtil.getUserId();

    User user = userService.findAndExistByUserId(userId);

    boolean isAutoFlag = autoTradingStrategyForm.getAutoFlag();

    user.updateAutoFlag(isAutoFlag);
    user.updateLpFlag(autoTradingStrategyForm.getLpFlag());

    if (isAutoFlag) {
      Optional<AutoTradingStrategy> autoTradingStrategyOptional =
          autoTradingStrategyService.findByUserId(userId);
      Symbol currentSymbol =
          symbolVariableService.findAndExistSymbolByName(autoTradingStrategyForm.getSymbol());

      if (autoTradingStrategyOptional.isPresent()) {
        AutoTradingStrategy autoTradingStrategy = autoTradingStrategyOptional.get();

        autoTradingStrategy.update(
            currentSymbol,
            autoTradingStrategyForm.getStopLossPercent(),
            autoTradingStrategyForm.getMinimumProfitTargetPercent(),
            autoTradingStrategyForm.getFixedProfitTargetPercent(),
            autoTradingStrategyForm.getDivisionCount(),
            autoTradingStrategyForm.getAdditionalBuyTargetPercent(),
            autoTradingStrategyForm.getEntryCandleMinutes(),
            autoTradingStrategyForm.getKneeEntryPercent(),
            autoTradingStrategyForm.getShoulderEntryPercent());
      } else {
        autoTradingStrategyService.create(
            user,
            currentSymbol,
            autoTradingStrategyForm.getStopLossPercent(),
            autoTradingStrategyForm.getMinimumProfitTargetPercent(),
            autoTradingStrategyForm.getFixedProfitTargetPercent(),
            autoTradingStrategyForm.getDivisionCount(),
            autoTradingStrategyForm.getAdditionalBuyTargetPercent(),
            autoTradingStrategyForm.getEntryCandleMinutes(),
            autoTradingStrategyForm.getKneeEntryPercent(),
            autoTradingStrategyForm.getShoulderEntryPercent());
      }
    }
    return autoTradingStrategyForm;
  }
}
