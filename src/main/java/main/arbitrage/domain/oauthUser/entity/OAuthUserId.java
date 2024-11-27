package main.arbitrage.domain.oauthUser.entity;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import main.arbitrage.domain.user.entity.User;

import java.io.Serializable;

@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class OAuthUserId implements Serializable {
    private String providerId;
    private String provider;
    private User user;
}