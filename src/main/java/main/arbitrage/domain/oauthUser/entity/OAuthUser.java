package main.arbitrage.domain.oauthUser.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import main.arbitrage.domain.user.entity.User;

@Entity
@Table(name = "oauth_user")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@IdClass(OAuthUserId.class)
public class OAuthUser {
    @Id
    @Column(name = "provider_id", nullable = false)
    private String providerId;

    @Id
    @Column(name = "provider", nullable = false, length = 6)
    private String provider;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "user_id", insertable = false, updatable = false)
    private Long userId;

    @Builder
    public OAuthUser(String providerId, String provider, User user) {
        this.providerId = providerId;
        this.provider = provider;
        this.user = user;
    }
}