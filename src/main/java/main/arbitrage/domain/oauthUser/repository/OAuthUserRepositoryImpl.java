package main.arbitrage.domain.oauthUser.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import main.arbitrage.domain.oauthUser.entity.OAuthUser;
import main.arbitrage.domain.oauthUser.entity.QOAuthUser;
import main.arbitrage.domain.user.entity.QUser;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class OAuthUserRepositoryImpl implements OauthUserQueryRepository {
  private final JPAQueryFactory queryFactory;
  private final QOAuthUser oAuthUser = QOAuthUser.oAuthUser;
  private final QUser user = QUser.user;

  @Override
  public Optional<OAuthUser> findByProviderAndProviderId(String provider, String providerId) {
    return Optional.ofNullable(
        queryFactory
            .selectFrom(oAuthUser)
            .innerJoin(oAuthUser.user, user)
            .fetchJoin()
            .where(oAuthUser.providerId.eq(providerId).and(oAuthUser.provider.eq(provider)))
            .fetchOne());
  }
}
