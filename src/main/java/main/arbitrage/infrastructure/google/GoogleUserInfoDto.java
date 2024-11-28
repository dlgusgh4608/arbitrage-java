package main.arbitrage.infrastructure.google;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class GoogleUserInfoDto {
    private String sub;
    private String email;

    @JsonProperty("email_verified")
    private boolean emailVerified;
}