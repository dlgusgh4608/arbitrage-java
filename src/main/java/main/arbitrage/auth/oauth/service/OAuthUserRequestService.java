package main.arbitrage.auth.oauth.service;

import lombok.RequiredArgsConstructor;
import main.arbitrage.auth.oauth.dto.CustomOAuthRequestDto;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
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
        String accessToken = userRequest.getAccessToken().getTokenValue();
        String providerId = extractProviderId(oAuth2User, provider);
        String email = extractEmail(oAuth2User, provider);

        Map<String, Object> attributes = Map.of(
                "provider", provider,
                "providerId", providerId,
                "accessToken", accessToken,
                "email", email
        );

        return new CustomOAuthRequestDto(
                Collections.singleton(new SimpleGrantedAuthority("ROLE_USER")),
                attributes,
                "providerId",
                provider,
                providerId,
                accessToken,
                email
        );
    }

    private String extractProviderId(OAuth2User oAuth2User, String provider) {
        return switch (provider.toLowerCase()) {
            case "google" -> oAuth2User.getAttribute("sub");
            case "kakao" -> String.valueOf(Optional.ofNullable(oAuth2User.getAttribute("id")).get());
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