package main.arbitrage.auth.oauth.dto;

import java.util.Collection;
import java.util.Map;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import lombok.Builder;
import lombok.Getter;

@Getter
public class CustomOAuthRequest extends DefaultOAuth2User {
    private final String provider;
    private final String providerId;
    private final String accessToken;
    private final String email;

    @Builder
    public CustomOAuthRequest(Collection<? extends GrantedAuthority> authorities,
            Map<String, Object> attributes, String nameAttributeKey, String provider,
            String providerId, String accessToken, String email) {
        super(authorities, attributes, nameAttributeKey);
        this.provider = provider;
        this.providerId = providerId;
        this.accessToken = accessToken;
        this.email = email;
    }
}
