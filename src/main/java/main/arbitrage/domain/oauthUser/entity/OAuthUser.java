package main.arbitrage.domain.oauthUser.entity;

import java.time.LocalDateTime;
import org.hibernate.annotations.CreationTimestamp;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
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
    @Column(name = "provider_id", nullable = false, columnDefinition = "VARCHAR(30)")
    private String providerId;

    @Id
    @Column(name = "provider", nullable = false, columnDefinition = "VARCHAR(6)")
    private String provider;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "user_id", insertable = false, updatable = false)
    private Long userId;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Builder
    public OAuthUser(String providerId, String provider, User user) {
        this.providerId = providerId;
        this.provider = provider;
        this.user = user;
    }
}
