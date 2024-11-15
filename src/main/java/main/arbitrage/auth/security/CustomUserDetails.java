package main.arbitrage.auth.security;


import main.arbitrage.auth.jwt.dto.JwtDto;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

public class CustomUserDetails implements UserDetails {
    private final Long userId;
    private final String email;
    private final String nickname;

    public CustomUserDetails(JwtDto tokenDto) {
        this.userId = tokenDto.getUserId();
        this.email = tokenDto.getEmail();
        this.nickname = tokenDto.getNickname();
    }

    @Override
    public String getUsername() {
        return "";
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singleton(new SimpleGrantedAuthority("ROLE_USER"));
    }

    @Override
    public String getPassword() {
        return "";
    }

    public Long getUserId() {
        return userId;
    }

    public String getEmail() {
        return email;
    }

    public String getNickname() {
        return nickname;
    }
}