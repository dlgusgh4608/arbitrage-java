package main.arbitrage.auth.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import main.arbitrage.auth.jwt.dto.JwtDto;
import main.arbitrage.auth.security.CustomUserDetails;
import main.arbitrage.infrastructure.redis.service.RefreshTokenService;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtFilter extends OncePerRequestFilter {

    private final JwtUtil jwtProvider;
    private final RefreshTokenService refreshTokenService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
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
             * */
            String accessToken = resolveAccessToken(request);

            // AccessToken null check
            if (accessToken == null) {
                log.info("access token is not found");
                throw new RuntimeException("invalid token");
            }

            // 이 토큰은 내가 만든 토큰인가?
            JwtDto tokenInfo = jwtProvider.getTokenInfo(accessToken);

            // 엑세스 토큰이 만료되지 않았을때.
            if (!tokenInfo.isExpired()) {
                saveUserAuthContext(tokenInfo);
                filterChain.doFilter(request, response);
                return;
            }

            /*
             * 해당 라인 아래부터는 Refresh Token을 이용해 Access Token을 재발급입니다.
             * */

            String refreshToken = resolveRefreshToken(request);

            // refresh token이 존재하지 않을때.
            if (refreshToken == null) {
                log.info("refresh token is not found");
                throw new RuntimeException("invalid token");
            }

            Long userId = tokenInfo.getUserId();
            String email = tokenInfo.getEmail();
            String nickname = tokenInfo.getNickname();

            // email을 통해 refresh token을 redis에서 받아옴.
            String foundRefreshToken = refreshTokenService.findRefreshToken(email);

            // redis에서 찾아온 refresh token이 없을때. (TTL이 지나 소멸 혹은 공격받음)
            if (foundRefreshToken == null) {
                log.info("refresh token expired");
                throw new RuntimeException("refresh token expired");
            }

            // redis에서 찾아온 refresh token과 header을 통해 받아온 refresh token이 일치하지 않음.
            if (!refreshToken.equals(foundRefreshToken)) {
                log.info("Refresh tokens of front and back do not match\n of client: {}\n of redis: {}", refreshToken, foundRefreshToken);
                throw new RuntimeException("invalid token");
            }

            /*
             * 해당 라인 아래부터는 refresh token의 TTL이 지나지 않았고
             * access token도 정상적인 key로 만들어졌을때
             * 새로운 access, refresh token을 만들고 response Header에 넣어주고 next.
             * */
            String newAccessToken = jwtProvider.createToken(userId, email, nickname);
            String newRefreshToken = refreshTokenService.updateRefreshToken(email, UUID.randomUUID().toString());

            JwtDto newTokenInfo = jwtProvider.getTokenInfo(newAccessToken);
            saveUserAuthContext(newTokenInfo);

            // new Cookie() client 작업 후 http only cookie 만들어 테스트

            response.setHeader("accessToken", newAccessToken);
            response.setHeader("refreshToken", newRefreshToken);
            log.info("New AccessToken: {}\nNew Refresh Token: {}", newAccessToken, newRefreshToken);

            filterChain.doFilter(request, response);
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write(e.getMessage());
            return;
        }
    }

    private void saveUserAuthContext(JwtDto tokenDto) {
        UserDetails userDetails = new CustomUserDetails(tokenDto);

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                );

        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    // SecurityConfig에 있는 requestMatchers와 일치시켜주세요.
    private boolean isPublicUrl(String requestURI) {
        return requestURI.equals("/api/users/login") ||
                requestURI.equals("/api/users/register") ||
                requestURI.equals("/") ||
                requestURI.startsWith("/css");
    }

    private String resolveRefreshToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("refreshToken");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    private String resolveAccessToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}