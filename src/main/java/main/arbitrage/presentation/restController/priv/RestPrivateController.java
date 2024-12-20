package main.arbitrage.presentation.restController.priv;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import main.arbitrage.application.order.service.OrderApplicationService;
import main.arbitrage.application.user.service.UserApplicationService;
import main.arbitrage.global.util.cookie.CookieUtil;
import main.arbitrage.presentation.dto.request.BuyOrderRequest;
import main.arbitrage.presentation.dto.request.EditUserNicknameRequest;
import main.arbitrage.presentation.restController.priv.constant.PrivateRestControllerUrlConstants;

@RestController
@RequestMapping(PrivateRestControllerUrlConstants.DEFAULT_URL)
@RequiredArgsConstructor
public class RestPrivateController {
    private final UserApplicationService userApplicationService;
    private final OrderApplicationService orderApplicationService;

    @PatchMapping(PrivateRestControllerUrlConstants.EDIT_NICKNAME)
    public ResponseEntity<?> editNicknamePatch(@Valid @RequestBody EditUserNicknameRequest req,
            HttpServletResponse response) {
        String accessToken = userApplicationService.updateNickname(req);
        CookieUtil.addCookie(response, "accessToken", accessToken, -1, true);
        return ResponseEntity.ok().build();
    }

    @PostMapping(PrivateRestControllerUrlConstants.BUY_ORDER)
    public ResponseEntity<?> postBuyOrder(@Valid @RequestBody BuyOrderRequest req)
            throws Exception {
        return ResponseEntity.ok(orderApplicationService.createBuyOrder(req));
    }
}
