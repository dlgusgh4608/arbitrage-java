package main.arbitrage.auth.oauth.repository;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import main.arbitrage.global.util.cookie.CookieUtil;
import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.SerializationUtils;


import java.util.Base64;
import java.util.UUID;


@Component
public class OAuthUserRequestRepository implements AuthorizationRequestRepository<OAuth2AuthorizationRequest> {
    public static final String OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME = UUID.randomUUID().toString();
    private static final int COOKIE_EXPIRE_SECONDS = 180;

    // 인증 서버는 사용자의 동의 항목에 대한 동의 여부에 따라 인가 코드 또는 에러 응답을 생성한다.
    @Override
    public OAuth2AuthorizationRequest loadAuthorizationRequest(HttpServletRequest request) {
        return CookieUtil.getCookie(request, OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME)
                .map(this::deserialize)
                .orElse(null);
    }

    // 인가 코드 발급 요청을 시작한 시점에 호출 되어, OAuth2AuthorizationRequest를 저장한다.
    @Override
    public void saveAuthorizationRequest(
            OAuth2AuthorizationRequest authorizationRequest,
            HttpServletRequest request,
            HttpServletResponse response
    ) {

        if (authorizationRequest == null) {
            CookieUtil.removeCookie(request, response, OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME);
            return;
        }

        CookieUtil.addCookie(
                response,
                OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME,
                serialize(authorizationRequest),
                COOKIE_EXPIRE_SECONDS,
                true
        );
    }

    @Override
    public OAuth2AuthorizationRequest removeAuthorizationRequest(
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        return this.loadAuthorizationRequest(request);
    }

    private String serialize(OAuth2AuthorizationRequest authorizationRequest) {
        try {
            return Base64.getEncoder().encodeToString(
                    SerializationUtils.serialize(authorizationRequest)
            );
        } catch (Exception e) {
            throw new RuntimeException("OAuth2 Serialize error", e);
        }
    }

    private OAuth2AuthorizationRequest deserialize(Cookie cookie) {
        try {
            return (OAuth2AuthorizationRequest) SerializationUtils.deserialize(
                    Base64.getDecoder().decode(cookie.getValue())
            );
        } catch (Exception e) {
            throw new RuntimeException("OAuth2 Deserialize error", e);
        }
    }
}