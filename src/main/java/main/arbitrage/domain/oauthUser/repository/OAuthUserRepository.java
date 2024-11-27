package main.arbitrage.domain.oauthUser.repository;

import main.arbitrage.domain.oauthUser.entity.OAuthUser;
import main.arbitrage.domain.oauthUser.entity.OAuthUserId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OAuthUserRepository extends JpaRepository<OAuthUser, OAuthUserId> {
    Optional<OAuthUser> findByProviderAndProviderId(String provider, String providerId);
}