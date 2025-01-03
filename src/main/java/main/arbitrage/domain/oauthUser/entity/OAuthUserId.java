package main.arbitrage.domain.oauthUser.entity;

import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import main.arbitrage.domain.user.entity.User;

@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class OAuthUserId implements Serializable {
  private String providerId;
  private String provider;
  private User user;
}
