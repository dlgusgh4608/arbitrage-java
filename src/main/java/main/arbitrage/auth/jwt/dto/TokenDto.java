package main.arbitrage.auth.jwt.dto;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TokenDto {
    private Long userId;
    private String email;
    private String nickname;
    private boolean isExpired;

    @Builder
    public TokenDto(Long userId, String email, String nickname, boolean isExpired) {
        this.userId = userId;
        this.email = email;
        this.nickname = nickname;
        this.isExpired = isExpired;
    }
}