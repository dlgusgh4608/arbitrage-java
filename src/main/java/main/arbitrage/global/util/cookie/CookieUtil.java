package main.arbitrage.global.util.cookie;

import java.util.Arrays;
import java.util.Optional;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import main.arbitrage.presentation.dto.response.UserTokenResponseCookie;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CookieUtil {
    public static Optional<Cookie> getCookie(HttpServletRequest request, String name) {
        return Optional.ofNullable(request.getCookies()).flatMap(cookies -> Arrays.stream(cookies)
                .filter(cookie -> cookie.getName().equals(name)).findFirst());
    }

    public static void addCookie(HttpServletResponse response, String name, String value,
            int maxAge, boolean httpOnly) {
        Cookie cookie = new Cookie(name, value);
        cookie.setPath("/");
        cookie.setHttpOnly(httpOnly);
        cookie.setMaxAge(maxAge);
        response.addCookie(cookie);
    }

    public static void removeCookie(HttpServletRequest request, HttpServletResponse response,
            String name) {
        Optional.ofNullable(request.getCookies()).ifPresent(cookies -> Arrays.stream(cookies)
                .filter(cookie -> cookie.getName().equals(name)).forEach(cookie -> {
                    cookie.setPath("/");
                    cookie.setMaxAge(0);
                    response.addCookie(cookie);
                }));
    }

    public static void setCookie(HttpServletResponse response,
            UserTokenResponseCookie userTokenResponseCookie) {
        CookieUtil.addCookie(response, "refreshToken", userTokenResponseCookie.getRefreshToken(),
                userTokenResponseCookie.getRefreshTokenTTL().intValue(), true);
        CookieUtil.addCookie(response, "accessToken", userTokenResponseCookie.getAccessToken(), -1,
                true);
    }

    public static void setCookie(HttpServletResponse response, String accessToken,
            String refreshToken, Long refreshTTL) {
        CookieUtil.addCookie(response, "refreshToken", refreshToken, refreshTTL.intValue(), true);
        CookieUtil.addCookie(response, "accessToken", accessToken, -1, true);
    }
}
