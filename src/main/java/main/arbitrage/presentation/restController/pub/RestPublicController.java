package main.arbitrage.presentation.restController.pub;

import jakarta.validation.Valid;
import main.arbitrage.presentation.restController.pub.constant.PublicRestControllerUrlConstants;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<?> postSendEmail(@Valid @RequestBody UserExistEmailReqDto request) throws Exception {
        String code = userApplicationService.sendEmail(
                EmailMessageDto.builder()
                        .to(request.getEmail())
                        .subject("[Arbitrage] 이메일 인증을 위한 인증 코드 발송")
                        .build()
        );
        return ResponseEntity.ok(UserExistEmailResDto.builder().code(code).build());
    }

    @PostMapping(PublicRestControllerUrlConstants.CHECK_CODE)
    public ResponseEntity<?> postCheckCode(@Valid @RequestBody UserCheckEmailCodeDto request) {
        boolean ok = userApplicationService.checkCode(request.getOriginCode(), request.getEncryptedCode());
        if (ok) {
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.badRequest().build();
        }
    }
}