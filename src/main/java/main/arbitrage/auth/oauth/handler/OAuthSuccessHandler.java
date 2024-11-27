package main.arbitrage.auth.oauth.handler;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import main.arbitrage.auth.jwt.JwtUtil;
import main.arbitrage.auth.oauth.dto.OAuthDto;
import main.arbitrage.auth.oauth.store.OAuthStore;
import main.arbitrage.infrastructure.redis.service.RefreshTokenService;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class OAuthSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
    private final JwtUtil jwtUtil;
    private final RefreshTokenService refreshTokenService;
    private final OAuthStore oAuthStore;

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException {
        OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();

        // OAuthUserService 에서 넘어옴.
        String provider = oauth2User.getAttribute("provider");
        String providerId = oauth2User.getAttribute("providerId");
        String email = oauth2User.getAttribute("email");

        OAuthDto oAuthDto = new OAuthDto(provider, providerId, email);

        String key = UUID.randomUUID().toString();

        oAuthStore.save(key, oAuthDto);

        String targetUrl = UriComponentsBuilder.fromUriString("/").queryParam("uid", key).build().toUriString();

        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}