package main.arbitrage.presentation.restController.pub;

import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import main.arbitrage.application.price.service.PriceApplicationService;
import main.arbitrage.application.user.service.UserApplicationService;
import main.arbitrage.global.util.aes.AESCrypto;
import main.arbitrage.presentation.dto.request.CheckCodeRequest;
import main.arbitrage.presentation.dto.request.SendEmailRequest;
import main.arbitrage.presentation.dto.response.ChartDataResponse;
import main.arbitrage.presentation.dto.response.SendEmailResponse;
import main.arbitrage.presentation.restController.pub.constant.PublicRestControllerUrlConstants;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(PublicRestControllerUrlConstants.DEFAULT_URL)
@RequiredArgsConstructor
public class RestPublicController {
  private final UserApplicationService userApplicationService;
  private final PriceApplicationService priceApplicationService;
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

  @GetMapping(PublicRestControllerUrlConstants.OHLC)
  public ResponseEntity<?> getMethodName(
      @RequestParam(name = "symbol", required = true, defaultValue = "BTC") String symbol,
      @RequestParam(name = "unit", required = true) int unit,
      @RequestParam(name = "lastTime", required = true) long lastTime) {

    List<ChartDataResponse> list = priceApplicationService.getOHLC(symbol, unit, lastTime);
    return ResponseEntity.ok(list);
  }
}
