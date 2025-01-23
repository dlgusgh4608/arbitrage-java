package main.arbitrage.domain.userEnv.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import main.arbitrage.application.auto.dto.AutomaticUserInfoDTO;
import main.arbitrage.application.auto.dto.QAutomaticUserInfoDTO;
import main.arbitrage.domain.autoTradingStrategy.entity.QAutoTradingStrategy;
import main.arbitrage.domain.symbol.entity.QSymbol;
import main.arbitrage.domain.user.entity.QUser;
import main.arbitrage.domain.userEnv.entity.QUserEnv;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class UserEnvQueryRepositoryImpl implements UserEnvQueryRepository {
  private final JPAQueryFactory queryFactory;
  private final QUserEnv userEnv = QUserEnv.userEnv;
  private final QUser user = QUser.user;
  private final QAutoTradingStrategy autoTradingStrategy = QAutoTradingStrategy.autoTradingStrategy;
  private final QSymbol symbol = QSymbol.symbol;

  @Override
  public List<AutomaticUserInfoDTO> findAutomaticUsers() {
    return queryFactory
        .select(
            new QAutomaticUserInfoDTO(
                user.id,
                user.autoFlag,
                userEnv.upbitAccessKey,
                userEnv.upbitSecretKey,
                userEnv.binanceAccessKey,
                userEnv.binanceSecretKey,
                autoTradingStrategy))
        .from(userEnv)
        .innerJoin(userEnv.user, user)
        .leftJoin(autoTradingStrategy)
        .on(autoTradingStrategy.user.eq(user))
        .leftJoin(symbol)
        .on(autoTradingStrategy.symbol.eq(symbol))
        .where(user.lpFlag.isTrue())
        .fetch();
  }
}
