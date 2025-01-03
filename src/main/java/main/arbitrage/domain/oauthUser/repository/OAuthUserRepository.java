package main.arbitrage.domain.oauthUser.repository;

import java.util.List;
import java.util.Optional;
import main.arbitrage.domain.oauthUser.entity.OAuthUser;
import main.arbitrage.domain.oauthUser.entity.OAuthUserId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface OAuthUserRepository extends JpaRepository<OAuthUser, OAuthUserId> {
  @Query(
      """
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
      @Param("provider") String provider, @Param("providerId") String providerId);

  List<OAuthUser> findByUserId(Long userId);
}
