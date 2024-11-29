package main.arbitrage.infrastructure.oauthValidator.kakao;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Builder
@Setter
public class KakaoUserInfoDto {
    private String email;
    private String providerId;
}