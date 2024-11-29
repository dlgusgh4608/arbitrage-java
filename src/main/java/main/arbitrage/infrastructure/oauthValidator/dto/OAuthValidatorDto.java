package main.arbitrage.infrastructure.oauthValidator.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Builder
@Setter
public class OAuthValidatorDto {
    private String email;
    private String providerId;
}