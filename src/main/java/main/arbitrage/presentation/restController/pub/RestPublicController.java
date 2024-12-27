package main.arbitrage.presentation.restController.pub;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import main.arbitrage.application.user.service.UserApplicationService;
import main.arbitrage.presentation.dto.request.CheckCodeRequest;
import main.arbitrage.presentation.dto.request.SendEmailRequest;
import main.arbitrage.presentation.dto.response.SendEmailResponse;
import main.arbitrage.presentation.restController.pub.constant.PublicRestControllerUrlConstants;

@RestController
@RequestMapping(PublicRestControllerUrlConstants.DEFAULT_URL)
@RequiredArgsConstructor
public class RestPublicController {
    private final UserApplicationService userApplicationService;

    @PostMapping(PublicRestControllerUrlConstants.SEND_EMAIL)
    public ResponseEntity<?> postSendEmail(@Valid @RequestBody SendEmailRequest request)
            throws Exception {
        String code = userApplicationService.sendEmail(request.email());
        return ResponseEntity.ok(SendEmailResponse.builder().code(code).build());
    }

    @PostMapping(PublicRestControllerUrlConstants.CHECK_CODE)
    public ResponseEntity<?> postCheckCode(@Valid @RequestBody CheckCodeRequest request) {
        boolean ok =
                userApplicationService.checkCode(request.originCode(), request.encryptedCode());
        if (ok) {
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.badRequest().build();
        }
    }
}
