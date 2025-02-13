package main.arbitrage.presentation.restController.pub;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import main.arbitrage.application.order.service.OrderApplicationService;
import main.arbitrage.application.user.service.UserApplicationService;
import main.arbitrage.global.util.aes.AESCrypto;
import main.arbitrage.presentation.dto.request.CheckCodeRequest;
import main.arbitrage.presentation.dto.request.SendEmailRequest;
import main.arbitrage.presentation.dto.response.SendEmailResponse;
import main.arbitrage.presentation.restController.pub.constant.PublicRestControllerUrlConstants;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(PublicRestControllerUrlConstants.DEFAULT_URL)
@RequiredArgsConstructor
public class RestPublicController {
  private final UserApplicationService userApplicationService;
  private final OrderApplicationService os;
  private final AESCrypto aesCrypto;

  @PostMapping(PublicRestControllerUrlConstants.SEND_EMAIL)
  public ResponseEntity<?> postSendEmail(@Valid @RequestBody SendEmailRequest req) {
    String code = userApplicationService.sendEmail(req.email());
    return ResponseEntity.ok(SendEmailResponse.builder().code(code).build());
  }

  @PostMapping(PublicRestControllerUrlConstants.CHECK_CODE)
  public ResponseEntity<?> postCheckCode(@Valid @RequestBody CheckCodeRequest req) {
    aesCrypto.check(req.encryptedCode(), req.originCode());
    return ResponseEntity.ok().build();
  }

  @GetMapping(PublicRestControllerUrlConstants.TEST)
  public ResponseEntity<?> test() {

    return ResponseEntity.ok(os.test());
  }
}
