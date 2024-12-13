package main.arbitrage.auth.jwt.dto;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Date;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class JwtDto {
    private Long userId;
    private String email;
    private String nickname;
    private Long expiredAt;

    @Builder
    public JwtDto(Long userId, String email, String nickname, Long expiredAt) {
        this.userId = userId;
        this.email = email;
        this.nickname = nickname;
        this.expiredAt = expiredAt;
    }
}