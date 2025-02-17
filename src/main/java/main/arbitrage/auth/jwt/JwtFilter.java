package main.arbitrage.auth.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import main.arbitrage.auth.dto.AuthContext;
import main.arbitrage.auth.exception.AuthErrorCode;
import main.arbitrage.auth.exception.AuthException;
import main.arbitrage.domain.grade.entity.GradeName;
import main.arbitrage.domain.tier.entity.TierName;
import main.arbitrage.global.exception.ErrorResponse;
import main.arbitrage.global.util.cookie.CookieUtil;
import main.arbitrage.infrastructure.redis.service.RefreshTokenService;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtFilter extends OncePerRequestFilter {
  private final JwtUtil jwtUtil;
  private final RefreshTokenService refreshTokenService;
  private final ObjectMapper objectMapper;

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {
    // Public URl check

    if (isPublicUrl(request.getRequestURI())) {
      filterChain.doFilter(request, response);
      return;
    }

    try {
      /*
       * 1. accessToken을 검사 -> 만료가 되었는가? 정상적인 토큰인가?
       * 2. 내가 만든 토큰이 아니면 return Error
       * 3. 만료가 되지 않았으면 next
       * 4. 만료가 되었으면, refreshToken을 검사
       * 5. refreshToken을 redis에서 찾아 일치하지 않으면 return Error
       * 6. refreshToken이 만료되었으면 return Error
       * 7. refreshToken이 만료가 되지 않았으면 refreshToken rolling, new accessToken 발급
       */
      Optional<Cookie> accessTokenCookie = CookieUtil.getCookie(request, "accessToken");

      // AccessToken is empty
      if (accessTokenCookie.isEmpty()) {
        logout(request, response);
        filterChain.doFilter(request, response);
        return;
      }

      String accessToken = accessTokenCookie.get().getValue();

      // 이 토큰은 내가 만든 토큰인가?
      AuthContext tokenInfo = jwtUtil.getTokenInfo(accessToken);

      // 현재 시각 초단위 ( JWT에 저장하면 초단위로 데이터를 출력한다. )
      long nowTimeToSec = Math.floorDiv(System.currentTimeMillis(), 1000);

      // 엑세스 토큰이 만료되지 않았을때. (토큰이 5분 이상 남았을때)
      if (nowTimeToSec < tokenInfo.getExpiredAt() - 60 * 5) {
        saveUserAuthContext(tokenInfo);
        filterChain.doFilter(request, response);
        return;
      }

      /*
       * 해당 라인 아래부터는 Refresh Token을 이용해 Access Token을 재발급입니다.
       */

      Optional<Cookie> refreshTokenCookie = CookieUtil.getCookie(request, "refreshToken");

      // refresh token이 존재하지 않을때.
      if (refreshTokenCookie.isEmpty())
        throw new AuthException(AuthErrorCode.REFRESH_TOKEN_NOT_FOUND, "리프레시 토큰이 존재하지 않습니다.");

      String refreshToken = refreshTokenCookie.get().getValue();

      Long userId = tokenInfo.getUserId();
      String email = tokenInfo.getEmail();
      String nickname = tokenInfo.getNickname();
      TierName tier = TierName.valueOf(tokenInfo.getTier());
      GradeName grade = GradeName.valueOf(tokenInfo.getGrade());

      // email을 통해 refresh token을 redis에서 받아옴.
      String foundRefreshToken = refreshTokenService.findRefreshToken(email);

      // redis에서 찾아온 refresh token이 없을때.
      if (foundRefreshToken == null)
        throw new AuthException(AuthErrorCode.EXPIRED_TOKEN, "리프레시 토큰이 만료되었습니다.");

      // redis에서 찾아온 refresh token과 header을 통해 받아온 refresh token이 일치하지 않음.
      if (!refreshToken.equals(foundRefreshToken))
        throw new AuthException(
            AuthErrorCode.REFRESH_TOKEN_MISMATCH,
            String.format("리프레시 토큰 불일치 - 클라이언트: %s, Redis: %s", refreshToken, foundRefreshToken));

      /*
       * 해당 라인 아래부터는 refresh token의 TTL이 지나지 않았고 access token도 정상적인 key로 만들어졌을때 새로운 access,
       * refresh token을 만들고 response Header 및 cookie 에 넣어주고 next.
       */
      String newAccessToken = jwtUtil.createToken(userId, email, nickname, tier, grade);
      Long refreshTokenTTL = refreshTokenService.getRefreshTokenTTL(email);
      String newRefreshToken =
          refreshTokenService.updateRefreshToken(
              email, UUID.randomUUID().toString(), refreshTokenTTL);

      CookieUtil.setCookie(response, newAccessToken, newRefreshToken, refreshTokenTTL);

      log.info("새로운 액세스 토큰: {}\n새로운 리프레시 토큰: {}", newAccessToken, newRefreshToken);

      AuthContext newTokenInfo = jwtUtil.getTokenInfo(newAccessToken);
      saveUserAuthContext(newTokenInfo);
      filterChain.doFilter(request, response);
    } catch (AuthException e) {
      handlerAuthException(request, response, e);
    } catch (Exception e) {
      handleUnknownException(request, response, e);
    }
  }

  private void handleUnknownException(
      HttpServletRequest request, HttpServletResponse response, Exception e) throws IOException {
    logout(request, response);
    response.setStatus(500);
    response.setContentType("application/json;charset=UTF-8");
    log.error(e.getMessage());

    ErrorResponse errorResponse =
        ErrorResponse.builder().code("G01").message("알 수 없는 에러입니다.").build();

    String jsonResponse = objectMapper.writeValueAsString(errorResponse);
    response.getWriter().write(jsonResponse);
  }

  private void handlerAuthException(
      HttpServletRequest request, HttpServletResponse response, AuthException e)
      throws IOException {
    logout(request, response);
    response.setStatus(e.getErrorCode().getHttpStatus().value());
    response.setContentType("application/json;charset=UTF-8");
    log.error(e.getMessage());

    ErrorResponse errorResponse = ErrorResponse.of(e);
    String jsonResponse = objectMapper.writeValueAsString(errorResponse);
    response.getWriter().write(jsonResponse);
  }

  private boolean isPublicUrl(String requestURI) {
    return requestURI.equals("/api/login") || requestURI.equals("/api/signup");
  }

  private void saveUserAuthContext(AuthContext tokenDto) {
    UsernamePasswordAuthenticationToken authentication =
        new UsernamePasswordAuthenticationToken(tokenDto, null, tokenDto.getAuthorities());

    SecurityContextHolder.getContext().setAuthentication(authentication);
  }

  private void logout(HttpServletRequest request, HttpServletResponse response) {
    CookieUtil.removeCookie(request, response, "accessToken");
    CookieUtil.removeCookie(request, response, "refreshToken");
    SecurityContextHolder.clearContext();
  }
}
