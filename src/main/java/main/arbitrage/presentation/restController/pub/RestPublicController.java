package main.arbitrage.presentation.restController.pub;

import main.arbitrage.presentation.restController.pub.constant.PublicRestControllerUrlConstants;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import main.arbitrage.application.user.service.UserApplicationService;
import main.arbitrage.domain.user.dto.UserCheckEmailCodeDto;
import main.arbitrage.domain.user.dto.UserExistEmailReqDto;
import main.arbitrage.domain.user.dto.UserExistEmailResDto;
import main.arbitrage.infrastructure.email.dto.EmailMessageDto;

@RestController
@RequestMapping(PublicRestControllerUrlConstants.DEFAULT_URL)
@RequiredArgsConstructor
public class RestPublicController {
    private final UserApplicationService userApplicationService;

    @PostMapping(PublicRestControllerUrlConstants.SEND_EMAIL)
    public ResponseEntity<?> postSendEmail(@RequestBody UserExistEmailReqDto request) throws Exception {
        String code = userApplicationService.sendEmail(
                EmailMessageDto.builder()
                        .to(request.getEmail())
                        .subject("[Arbitrage] 이메일 인증을 위한 인증 코드 발송")
                        .build()
        );
        return ResponseEntity.ok(UserExistEmailResDto.builder().code(code).build());
    }

    @PostMapping(PublicRestControllerUrlConstants.CHECK_CODE)
    public ResponseEntity<?> postCheckCode(@RequestBody UserCheckEmailCodeDto request) throws Exception {
        boolean ok = userApplicationService.checkCode(request.getOriginCode(), request.getEncryptedCode());
        if (ok) {
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.badRequest().build();
        }
    }
}