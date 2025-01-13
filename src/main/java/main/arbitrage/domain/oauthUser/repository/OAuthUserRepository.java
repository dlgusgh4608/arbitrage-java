package main.arbitrage.domain.oauthUser.repository;

import java.util.List;
import java.util.Optional;
import main.arbitrage.domain.oauthUser.entity.OAuthUser;
import main.arbitrage.domain.oauthUser.entity.OAuthUserId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OAuthUserRepository
    extends JpaRepository<OAuthUser, OAuthUserId>, OauthUserQueryRepository {

  @Override
  public Optional<OAuthUser> findByProviderAndProviderId(String provider, String providerId);

  List<OAuthUser> findByUserId(Long userId);
}
