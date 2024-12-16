package main.arbitrage.auth.dto;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AuthContext implements UserDetails {
    private Long userId;
    private String email;
    private String nickname;
    private Long expiredAt;

    @Builder
    public AuthContext(Long userId, String email, String nickname, Long expiredAt) {
        this.userId = userId;
        this.email = email;
        this.nickname = nickname;
        this.expiredAt = expiredAt;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of();
    }

    @Override
    public String getPassword() {
        return "";
    }

    @Override
    public String getUsername() {
        return "";
    }
}