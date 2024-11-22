package main.arbitrage.presentation.api.user;

import java.io.IOException;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import main.arbitrage.application.user.service.UserApplicationService;
import main.arbitrage.domain.email.entity.EmailMessage;
import main.arbitrage.domain.user.dto.request.UserLoginRequest;
import main.arbitrage.domain.user.dto.request.UserRegisterRequest;
import main.arbitrage.domain.user.dto.request.UserSendMailRequest;
import main.arbitrage.domain.user.dto.response.UserTokenResponse;
import main.arbitrage.domain.user.dto.response.UserSendMailResponse;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final UserApplicationService userApplicationService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody UserRegisterRequest request, HttpServletResponse response) throws IOException {        
        UserTokenResponse userTokenResponse = userApplicationService.register(request);
        setCookies(userTokenResponse, response);

        return ResponseEntity.status(302).header("Location", "/").build();
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody UserLoginRequest request, HttpServletResponse response) {
        UserTokenResponse userTokenResponse = userApplicationService.login(request);
        setCookies(userTokenResponse, response);
        
        return ResponseEntity.status(302).header("Location", "/").build();
    }

    @PostMapping("/send-email")
    public ResponseEntity<?> sendMail(@RequestBody UserSendMailRequest request) throws Exception {
        String email = request.getEmail();
        userApplicationService.validateEmail(email);

        EmailMessage emailMessage = EmailMessage.builder()
                .to(email)
                .subject("[Arbitrage] 이메일 인증을 위한 인증 코드 발송")
                .build();

        String code = userApplicationService.sendEmail(emailMessage);

        return ResponseEntity.ok(UserSendMailResponse.builder().code(code).build());
    }

    private void setCookies(UserTokenResponse userTokenResponse, HttpServletResponse response) {
        Cookie refreshTokenCookie = new Cookie("refreshToken", userTokenResponse.getRefreshToken());
        refreshTokenCookie.setHttpOnly(true);
        refreshTokenCookie.setPath("/");
        refreshTokenCookie.setMaxAge(userTokenResponse.getRefreshTokenTTL().intValue());

        Cookie accessTokenCookie = new Cookie("accessToken", userTokenResponse.getAccessToken());
        accessTokenCookie.setPath("/");
        accessTokenCookie.setMaxAge(-1);
        
        response.addCookie(refreshTokenCookie);
        response.addCookie(accessTokenCookie);
    }
}