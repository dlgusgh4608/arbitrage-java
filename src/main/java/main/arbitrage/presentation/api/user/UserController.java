package main.arbitrage.presentation.api.user;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import main.arbitrage.application.user.service.UserApplicationService;
import main.arbitrage.domain.oauthUser.dto.OAuthUserRegisterRequest;
import main.arbitrage.domain.user.dto.UserCheckEmailCodeDto;
import main.arbitrage.domain.user.dto.UserLoginDto;
import main.arbitrage.domain.user.dto.UserRegisterDto;
import main.arbitrage.domain.user.dto.UserExistEmailReqDto;
import main.arbitrage.domain.user.dto.UserExistEmailResDto;
import main.arbitrage.domain.user.dto.UserTokenDto;
import main.arbitrage.global.util.cookie.CookieUtil;
import main.arbitrage.infrastructure.email.dto.EmailMessageDto;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final UserApplicationService userApplicationService;

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody UserRegisterDto request, HttpServletResponse response) throws Exception {
        UserTokenDto userTokenDto = userApplicationService.register(request);
        setCookies(userTokenDto, response);

        return ResponseEntity.status(302).header("Location", "/").build();
    }

    @PostMapping("/signup/oauth")
    public ResponseEntity<?> oAuthSignup(@RequestBody OAuthUserRegisterRequest request, HttpServletResponse response) throws Exception {
        UserTokenDto userTokenDto = userApplicationService.oAuthUserRegister(request);
        setCookies(userTokenDto, response);

        return ResponseEntity.status(302).header("Location", "/").build();
    }

    @PostMapping("/signup/send-email")
    public ResponseEntity<?> sendEmail(@RequestBody UserExistEmailReqDto request) throws Exception {
        String code = userApplicationService.sendEmail(
                EmailMessageDto.builder()
                        .to(request.getEmail())
                        .subject("[Arbitrage] 이메일 인증을 위한 인증 코드 발송")
                        .build()
        );

        return ResponseEntity.ok(UserExistEmailResDto.builder().code(code).build());
    }

    @PostMapping("/signup/check-code")
    public ResponseEntity<?> checkCode(@RequestBody UserCheckEmailCodeDto request) throws Exception {
        boolean ok = userApplicationService.checkCode(request.getOriginCode(), request.getEncryptedCode());


        if (ok) {
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody UserLoginDto request, HttpServletResponse response) {
        UserTokenDto userTokenDto = userApplicationService.login(request);
        setCookies(userTokenDto, response);

        return ResponseEntity.status(302).header("Location", "/").build();
    }

    private void setCookies(UserTokenDto userTokenDto, HttpServletResponse response) {
        CookieUtil.addCookie(
                response,
                "refreshToken",
                userTokenDto.getRefreshToken(),
                userTokenDto.getRefreshTokenTTL().intValue(),
                true
        );
        CookieUtil.addCookie(
                response,
                "accessToken",
                userTokenDto.getAccessToken(),
                -1,
                true
        );
    }
}