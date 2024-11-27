package main.arbitrage.auth.jwt.dto;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class JwtDto {
    private Long userId;
    private String email;
    private boolean isExpired;

    @Builder
    public JwtDto(Long userId, String email, boolean isExpired) {
        this.userId = userId;
        this.email = email;
        this.isExpired = isExpired;
    }
}