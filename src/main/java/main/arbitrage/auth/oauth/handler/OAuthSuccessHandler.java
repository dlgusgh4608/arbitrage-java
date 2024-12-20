package main.arbitrage.auth.oauth.handler;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import main.arbitrage.auth.jwt.JwtUtil;
import main.arbitrage.auth.oauth.dto.CustomOAuthRequestDto;
import main.arbitrage.domain.oauthUser.dto.OAuthUserDto;
import main.arbitrage.domain.oauthUser.entity.OAuthUser;
import main.arbitrage.domain.oauthUser.repository.OAuthUserRepository;
import main.arbitrage.domain.oauthUser.store.OAuthUserStore;
import main.arbitrage.domain.user.entity.User;
import main.arbitrage.domain.user.repository.UserRepository;
import main.arbitrage.global.util.cookie.CookieUtil;
import main.arbitrage.infrastructure.redis.service.RefreshTokenService;

@Service
@RequiredArgsConstructor
public class OAuthSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
    private final JwtUtil jwtUtil;
    private final RefreshTokenService refreshTokenService;
    private final OAuthUserStore oAuthUserStore;
    private final UserRepository userRepository;
    private final OAuthUserRepository oAuthUserRepository;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
            Authentication authentication) throws IOException {
        CustomOAuthRequestDto oauth2User = (CustomOAuthRequestDto) authentication.getPrincipal();

        String provider = oauth2User.getProvider();
        String providerId = oauth2User.getProviderId();
        String email = oauth2User.getEmail();
        String accessToken = oauth2User.getAccessToken();

        /*
         * provider, providerId 를 이용하여 OAuthUser를 찾음 OAuthUser가 없다 -> 소셜로그인 처음 -> OAuth 서버에서 받아온
         * Email기준으로 User 검사 ---> Naver의 경우 Public Email을 사용해 사용자가 Email을 변경할 수 있기에 제외 -> 같은 이메일을
         * 사용하는 User가 있을경우 OAuthUser와 User를 FK를 이용해 연결, Cookie에 Jwt등록 후 메인 페이지 redirect -> 같은 이메일을
         * 사용하는 User가 없을경우 OAuth서버에서 받아온 데이터를 넘겨 회원가입 페이지로 redircet -> 회원가입 페이지에서 Email을
         * disabled시키고(이미 이메일 검증이 완료됨) Password를 받아 User와 OAuthUser를 생성. OAuthUser가 있다 -> Cookie에
         * jwt 등록한 후 메인페이지 redirect
         */

        Optional<OAuthUser> oAuthUser =
                oAuthUserRepository.findByProviderAndProviderId(provider, providerId);

        if (oAuthUser.isEmpty()) {
            OAuthUserDto oAuthUserDto = new OAuthUserDto(provider, providerId, email, accessToken);
            emptyUserProcess(request, response, oAuthUserDto);
            return;
        }

        User user = oAuthUser.get().getUser();
        createJwtAndSetCookie(response, user);
        getRedirectStrategy().sendRedirect(request, response, "/");
    }

    private void emptyUserProcess(HttpServletRequest request, HttpServletResponse response,
            OAuthUserDto oAuthUserDto) throws IOException {
        Optional<User> user = userRepository.findByEmail(oAuthUserDto.getEmail());

        if (user.isEmpty()) { // User와 OAuthUser가 모두 없을 경우
            String key = UUID.randomUUID().toString();
            oAuthUserStore.save(key, oAuthUserDto);
            String targetUrl = UriComponentsBuilder.fromUriString("/signup").queryParam("uid", key)
                    .build().toUriString();
            getRedirectStrategy().sendRedirect(request, response, targetUrl);
            return;
        }
        // 가입된 이메일인데 User와 연동되지 않은경우
        OAuthUser oauthUser = OAuthUser.builder().provider(oAuthUserDto.getProvider())
                .providerId(oAuthUserDto.getProviderId()).user(user.get()).build();

        oAuthUserRepository.save(oauthUser);

        createJwtAndSetCookie(response, user.get());
        getRedirectStrategy().sendRedirect(request, response, "/");

    }

    private void createJwtAndSetCookie(HttpServletResponse response, User user) {
        Long userId = user.getId();
        String email = user.getEmail();
        String nickname = user.getNickname();

        String accessToken = jwtUtil.createToken(userId, email, nickname);
        Long refreshTokenTTL = refreshTokenService.getRefreshTokenTTL(email);
        String refreshToken = refreshTokenService.updateRefreshToken(email,
                UUID.randomUUID().toString(), refreshTokenTTL);

        CookieUtil.setCookie(response, accessToken, refreshToken, refreshTokenTTL);
    }
}
