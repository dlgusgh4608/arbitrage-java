package main.arbitrage.application.auto.service;

import jakarta.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import main.arbitrage.application.auto.dto.AutomaticUserInfoDTO;
import main.arbitrage.application.collector.dto.PremiumDTO;
import main.arbitrage.application.collector.service.CollectorScheduleService;
import main.arbitrage.auth.security.SecurityUtil;
import main.arbitrage.domain.buyOrder.service.BuyOrderService;
import main.arbitrage.domain.exchangeRate.service.ExchangeRateService;
import main.arbitrage.domain.price.service.PriceService;
import main.arbitrage.domain.sellOrder.service.SellOrderService;
import main.arbitrage.domain.symbol.entity.Symbol;
import main.arbitrage.domain.symbol.service.SymbolVariableService;
import main.arbitrage.domain.tradingStrategy.entity.TradingStrategy;
import main.arbitrage.domain.tradingStrategy.service.TradingStrategyService;
import main.arbitrage.domain.user.entity.User;
import main.arbitrage.domain.user.service.UserService;
import main.arbitrage.domain.userEnv.entity.UserEnv;
import main.arbitrage.domain.userEnv.service.UserEnvService;
import main.arbitrage.global.util.aes.AESCrypto;
import main.arbitrage.infrastructure.binance.BinancePublicRestService;
import main.arbitrage.infrastructure.binance.dto.response.BinanceExchangeInfoResponse;
import main.arbitrage.infrastructure.binance.websocket.BinanceUserStream;
import main.arbitrage.infrastructure.exchanges.ExchangePrivateRestFactory;
import main.arbitrage.infrastructure.exchanges.dto.ExchangePrivateRestPair;
import main.arbitrage.infrastructure.websocket.handler.BinanceUserStreamHandler;
import main.arbitrage.presentation.dto.form.TradingStrategyForm;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AutoApplicationService {
  private final TradingStrategyService autoTradingStrategyService;
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
  private Map<String, BinanceExchangeInfoResponse> binanceExchangeInfoMap =
      CollectorScheduleService.binanceExchangeInfoMap;

  @EventListener
  public void premiumConsumer(PremiumDTO dto) {
    userStreams.values().forEach(v -> v.run(dto));
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
      createUserStream(automaticUser);
    }
  }

  @Scheduled(fixedDelay = 1000 * 60 * 30) // 30min
  public void listenKeyUpdate() {
    for (BinanceUserStream userStream : userStreams.values()) {
      System.out.println("update listen key");
      userStream.updateListenKey();
    }
  }

  private void createUserStream(AutomaticUserInfoDTO automaticUser) {
    ExchangePrivateRestPair exchangePrivateServicePair =
        exchangePrivateRestFactory.create(
            aesCrypto.decrypt(automaticUser.upbitAccessKey()),
            aesCrypto.decrypt(automaticUser.upbitSecretKey()),
            aesCrypto.decrypt(automaticUser.binanceAccessKey()),
            aesCrypto.decrypt(automaticUser.binanceSecretKey()));

    BinanceUserStream userStream =
        new BinanceUserStream(
            automaticUser,
            binanceExchangeInfoMap,
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

  private void createUserStream(User user, UserEnv userEnv) {
    createUserStream(
        new AutomaticUserInfoDTO(
            user.getId(),
            user.isAutoFlag(),
            userEnv.getUpbitAccessKey(),
            userEnv.getUpbitSecretKey(),
            userEnv.getBinanceAccessKey(),
            userEnv.getBinanceSecretKey(),
            null));
  }

  private void createUserStream(User user, UserEnv userEnv, TradingStrategy tradingStrategy) {
    createUserStream(
        new AutomaticUserInfoDTO(
            user.getId(),
            user.isAutoFlag(),
            userEnv.getUpbitAccessKey(),
            userEnv.getUpbitSecretKey(),
            userEnv.getBinanceAccessKey(),
            userEnv.getBinanceSecretKey(),
            tradingStrategy));
  }

  private void updateUserStream(
      BinanceUserStream userStream, User user, UserEnv userEnv, TradingStrategy tradingStrategy) {
    userStream.updateAutomaticValue(
        binanceExchangeInfoMap,
        new AutomaticUserInfoDTO(
            user.getId(),
            user.isAutoFlag(),
            userEnv.getUpbitAccessKey(),
            userEnv.getUpbitSecretKey(),
            userEnv.getBinanceAccessKey(),
            userEnv.getBinanceSecretKey(),
            tradingStrategy));
  }

  @Transactional
  public TradingStrategyForm getAutoTradingStrategyForm() {
    Long userId = SecurityUtil.getUserId();
    User user = userService.findAndExistByUserId(userId);
    Optional<TradingStrategy> tradingStrategy = autoTradingStrategyService.findByUserId(userId);

    if (tradingStrategy.isPresent()) {
      return TradingStrategyForm.fromEntity(user, tradingStrategy.get());
    }

    return TradingStrategyForm.fromEntity(user);
  }

  @Transactional
  public TradingStrategyForm updateAutoTradingSetting(TradingStrategyForm autoTradingStrategyForm) {
    Long userId = SecurityUtil.getUserId();
    User user = userService.findAndExistByUserId(userId);
    userFlagUpdate(user, autoTradingStrategyForm);

    BinanceUserStream userStream = userStreams.get(userId);

    /*
     * 청산방지가 false인지 true인지 1차 선별
     * 만약 청산방지가 false인 경우 자동거래도 false이므로 userStream을 찾아 종료만 하면 됨.
     *
     * 자동거래가 false인지 true인지 2차 선별
     * 자동거래가 false이고 userStream이 null이면 userStream을 빈값으로 생성
     *
     * 자동거래가 true일 경우
     * userStream이 null이면 업데이트하는 userStream을 생성
     * userStream이 null이 아니면 이미 있는 userStream을 업데이트
     */

    if (!user.isLpFlag() && userStream != null) {
      userStreams.remove(userId);
      userStream.streamShutdown();

      return autoTradingStrategyForm;
    }

    UserEnv userEnv = userEnvService.findAndExistByUserId(userId);
    if (!user.isAutoFlag()) {
      if (userStream == null) {
        createUserStream(user, userEnv);
      } else {
        userStream.shutdown();
      }
      return autoTradingStrategyForm;
    }

    Optional<TradingStrategy> autoTradingStrategyOptional =
        autoTradingStrategyService.findByUserId(userId);

    Symbol symbol =
        symbolVariableService.findAndExistSymbolByName(autoTradingStrategyForm.getSymbol());

    TradingStrategy tradingStrategy =
        createOrUpdateAutoStrategy(
            autoTradingStrategyOptional, user, symbol, autoTradingStrategyForm);
    if (userStream == null) {
      createUserStream(user, userEnv, tradingStrategy);
    } else {
      updateUserStream(userStream, user, userEnv, tradingStrategy);
    }

    return autoTradingStrategyForm;
  }

  private void userFlagUpdate(User user, TradingStrategyForm autoTradingStrategyForm) {
    user.updateAutoFlag(autoTradingStrategyForm.getAutoFlag());
    user.updateLpFlag(autoTradingStrategyForm.getLpFlag());
  }

  private TradingStrategy createOrUpdateAutoStrategy(
      Optional<TradingStrategy> autoTradingStrategyOptional,
      User user,
      Symbol symbol,
      TradingStrategyForm autoTradingStrategyForm) {
    if (autoTradingStrategyOptional.isPresent()) {
      TradingStrategy tradingStrategy = autoTradingStrategyOptional.get();
      updateAutoStrategy(user, tradingStrategy, symbol, autoTradingStrategyForm);
      return tradingStrategy;
    }

    return createAutoStrategy(user, symbol, autoTradingStrategyForm);
  }

  private void updateAutoStrategy(
      User user,
      TradingStrategy tradingStrategy,
      Symbol symbol,
      TradingStrategyForm autoTradingStrategyForm) {
    tradingStrategy.update(
        symbol,
        autoTradingStrategyForm.getLeverage(),
        autoTradingStrategyForm.getStopLossPercent(),
        autoTradingStrategyForm.getMinimumProfitTargetPercent(),
        autoTradingStrategyForm.getFixedProfitTargetPercent(),
        autoTradingStrategyForm.getDivisionCount(),
        autoTradingStrategyForm.getAdditionalBuyTargetPercent(),
        autoTradingStrategyForm.getEntryCandleMinutes(),
        autoTradingStrategyForm.getKneeEntryPercent(),
        autoTradingStrategyForm.getShoulderEntryPercent());
  }

  private TradingStrategy createAutoStrategy(
      User user, Symbol symbol, TradingStrategyForm autoTradingStrategyForm) {
    return autoTradingStrategyService.create(
        user,
        symbol,
        autoTradingStrategyForm.getLeverage(),
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
