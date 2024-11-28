package main.arbitrage.domain.oauthUser.repository;

import main.arbitrage.domain.oauthUser.entity.OAuthUser;
import main.arbitrage.domain.oauthUser.entity.OAuthUserId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OAuthUserRepository extends JpaRepository<OAuthUser, OAuthUserId> {
    @Query("""
            SELECT
              oauth
            FROM
              OAuthUser AS oauth
              JOIN FETCH oauth.user AS user
            WHERE
              oauth.providerId = :providerId AND
              oauth.provider = :provider
            """)
    Optional<OAuthUser> findByProviderAndProviderId(
            @Param("provider") String provider,
            @Param("providerId") String providerId
    );
}