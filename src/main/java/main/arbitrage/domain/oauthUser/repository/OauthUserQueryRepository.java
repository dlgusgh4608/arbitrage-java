package main.arbitrage.domain.oauthUser.repository;

import java.util.Optional;
import main.arbitrage.domain.oauthUser.entity.OAuthUser;

public interface OauthUserQueryRepository {
  public Optional<OAuthUser> findByProviderAndProviderId(String provider, String providerId);
}
