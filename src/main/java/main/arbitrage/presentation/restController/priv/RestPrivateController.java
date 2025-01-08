package main.arbitrage.presentation.restController.priv;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import main.arbitrage.application.order.service.OrderApplicationService;
import main.arbitrage.application.user.service.UserApplicationService;
import main.arbitrage.global.util.cookie.CookieUtil;
import main.arbitrage.presentation.dto.request.EditUserNicknameRequest;
import main.arbitrage.presentation.dto.request.OrderRequest;
import main.arbitrage.presentation.dto.request.UpdateLeverageRequest;
import main.arbitrage.presentation.dto.request.UpdateMarginTypeRequest;
import main.arbitrage.presentation.restController.priv.constant.PrivateRestControllerUrlConstants;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(PrivateRestControllerUrlConstants.DEFAULT_URL)
@RequiredArgsConstructor
public class RestPrivateController {
  private final UserApplicationService userApplicationService;
  private final OrderApplicationService orderApplicationService;

  @PatchMapping(PrivateRestControllerUrlConstants.EDIT_NICKNAME)
  public ResponseEntity<?> editNicknamePatch(
      @Valid @RequestBody EditUserNicknameRequest req, HttpServletResponse response) {
    String accessToken = userApplicationService.updateNickname(req);
    CookieUtil.addCookie(response, "accessToken", accessToken, -1, true);
    return ResponseEntity.ok().build();
  }

  @PostMapping(PrivateRestControllerUrlConstants.BUY_ORDER)
  public ResponseEntity<?> postBuyOrder(@Valid @RequestBody OrderRequest req) {
    return ResponseEntity.ok(orderApplicationService.createBuyOrder(req));
  }

  @PostMapping(PrivateRestControllerUrlConstants.SELL_ORDER)
  public ResponseEntity<?> postSellOrder(@Valid @RequestBody OrderRequest req) {
    return ResponseEntity.ok(orderApplicationService.createSellOrder(req));
  }

  @PatchMapping(PrivateRestControllerUrlConstants.UPDATE_LEVERAGE)
  public ResponseEntity<?> patchLeverage(@Valid @RequestBody UpdateLeverageRequest req) {
    return ResponseEntity.ok(orderApplicationService.updateLeverage(req));
  }

  @PatchMapping(PrivateRestControllerUrlConstants.MARGIN_MODE)
  public ResponseEntity<?> patchLeverage(@Valid @RequestBody UpdateMarginTypeRequest req) {
    return ResponseEntity.ok(orderApplicationService.updateMarginType(req));
  }
}
