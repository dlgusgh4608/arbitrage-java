package main.arbitrage.presentation.api.user;

import java.io.IOException;

import main.arbitrage.domain.user.dto.request.UserCheckMailRequest;
import main.arbitrage.global.util.cookie.CookieUtil;
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

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody UserRegisterRequest request, HttpServletResponse response) throws Exception {
        UserTokenResponse userTokenResponse = userApplicationService.register(request);
        setCookies(userTokenResponse, response);

        return ResponseEntity.status(302).header("Location", "/").build();
    }

    @PostMapping("/signup/oauth")
    public ResponseEntity<?> oAuthSignup(@RequestBody UserRegisterRequest request, HttpServletResponse response) throws Exception {
        UserTokenResponse userTokenResponse = userApplicationService.register(request);
        setCookies(userTokenResponse, response);

        return ResponseEntity.status(302).header("Location", "/").build();
    }

    @PostMapping("/signup/send-email")
    public ResponseEntity<?> sendEmail(@RequestBody UserSendMailRequest request) throws Exception {
        String email = request.getEmail();
        userApplicationService.validateEmail(email);

        EmailMessage emailMessage = EmailMessage.builder()
                .to(email)
                .subject("[Arbitrage] 이메일 인증을 위한 인증 코드 발송")
                .build();

        String code = userApplicationService.sendEmail(emailMessage);

        return ResponseEntity.ok(UserSendMailResponse.builder().code(code).build());
    }

    @PostMapping("/signup/check-code")
    public ResponseEntity<?> checkCode(@RequestBody UserCheckMailRequest request) throws Exception {
        System.out.println(request.getOriginCode());
        System.out.println(request.getEncryptedCode());

        boolean ok = userApplicationService.checkCode(request.getOriginCode(), request.getEncryptedCode());


        if (ok) {
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody UserLoginRequest request, HttpServletResponse response) {
        UserTokenResponse userTokenResponse = userApplicationService.login(request);
        setCookies(userTokenResponse, response);

        return ResponseEntity.status(302).header("Location", "/").build();
    }

    private void setCookies(UserTokenResponse userTokenResponse, HttpServletResponse response) {
        CookieUtil.addCookie(
                response,
                "refreshToken",
                userTokenResponse.getRefreshToken(),
                userTokenResponse.getRefreshTokenTTL().intValue(),
                true
        );
        CookieUtil.addCookie(
                response,
                "accessToken",
                userTokenResponse.getAccessToken(),
                -1,
                true
        );
    }
}