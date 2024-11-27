package main.arbitrage.domain.oauthUser.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class OAuthUserRequestService extends DefaultOAuth2UserService {

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        String provider = userRequest.getClientRegistration().getRegistrationId();
        String providerId = extractProviderId(oAuth2User, provider);
        String email = extractEmail(oAuth2User, provider);

        Map<String, Object> customAttributes = new HashMap<>();
        customAttributes.put("provider", provider);
        customAttributes.put("providerId", providerId);
        customAttributes.put("email", email);

        return new DefaultOAuth2User(
                Collections.singleton(new SimpleGrantedAuthority("ROLE_USER")),
                customAttributes,
                "email"
        );
    }

    private String extractProviderId(OAuth2User oAuth2User, String provider) {
        return switch (provider.toLowerCase()) {
            case "google" -> oAuth2User.getAttribute("sub");
            case "kakao" -> oAuth2User.getAttribute("id");
            default -> null;
        };
    }

    private String extractEmail(OAuth2User oAuth2User, String provider) {
        return switch (provider.toLowerCase()) {
            case "google" -> oAuth2User.getAttribute("email");
            case "kakao" -> {
                Map<String, Object> response = oAuth2User.getAttribute("kakao_account");
                yield String.valueOf(response.get("email"));
            }
            default -> null;
        };
    }
}