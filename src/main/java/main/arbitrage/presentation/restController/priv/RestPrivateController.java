package main.arbitrage.presentation.restController.priv;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import main.arbitrage.domain.buyOrder.dto.BuyOrderReqDto;
import main.arbitrage.domain.buyOrder.dto.BuyOrderResDto;
import main.arbitrage.domain.user.dto.UserEditNicknameDto;
import main.arbitrage.global.util.cookie.CookieUtil;
import main.arbitrage.presentation.restController.priv.constant.PrivateRestControllerUrlConstants;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;
import main.arbitrage.application.order.OrderApplicationService;
import main.arbitrage.application.user.service.UserApplicationService;

@RestController
@RequestMapping(PrivateRestControllerUrlConstants.DEFAULT_URL)
@RequiredArgsConstructor
public class RestPrivateController {
    private final UserApplicationService userApplicationService;
    private final OrderApplicationService orderApplicationService;

    @PatchMapping(PrivateRestControllerUrlConstants.EDIT_NICKNAME)
    public ResponseEntity<?> editNicknamePatch(@Valid @RequestBody UserEditNicknameDto req, HttpServletResponse response) {
        String accessToken = userApplicationService.updateNickname(req);
        CookieUtil.addCookie(response, "accessToken", accessToken, -1, true);
        return ResponseEntity.ok().build();
    }

    @PostMapping(PrivateRestControllerUrlConstants.BUY_ORDER)
    public ResponseEntity<?> postBuyOrder(@Valid @RequestBody BuyOrderReqDto req) throws Exception {
        BuyOrderResDto buyOrder = orderApplicationService.createBuyOrder(req);
        return ResponseEntity.ok(buyOrder);
    }
}