package main.arbitrage.presentation.controller.pub;

import java.util.List;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import main.arbitrage.application.collector.service.CollectorScheduleService;
import main.arbitrage.application.order.service.OrderApplicationService;
import main.arbitrage.application.price.service.PriceApplicationService;
import main.arbitrage.application.user.service.UserApplicationService;
import main.arbitrage.auth.dto.AuthContext;
import main.arbitrage.domain.oauthUser.store.OAuthUserStore;
import main.arbitrage.domain.symbol.service.SymbolVariableService;
import main.arbitrage.global.util.cookie.CookieUtil;
import main.arbitrage.infrastructure.exchange.binance.dto.response.BinanceExchangeInfoResponse;
import main.arbitrage.presentation.controller.pub.constant.PublicControllerUrlConstants;
import main.arbitrage.presentation.dto.form.UserLoginForm;
import main.arbitrage.presentation.dto.form.UserSignupForm;
import main.arbitrage.presentation.dto.response.UserTokenResponseCookie;
import main.arbitrage.presentation.dto.view.OAuthSignupView;
import main.arbitrage.presentation.dto.view.PriceView;
import main.arbitrage.presentation.dto.view.UserTradeInfo;

@Controller
@RequestMapping(PublicControllerUrlConstants.DEFAULT_URL)
@RequiredArgsConstructor
public class PublicController {
    private final UserApplicationService userApplicationService;
    private final SymbolVariableService symbolVariableService;
    private final OAuthUserStore authUserStore;
    private final PriceApplicationService priceApplicationService;
    private final CollectorScheduleService collectorScheduleService;
    private final OrderApplicationService orderApplicationService;

    /**
     * only public
     */
    @GetMapping(PublicControllerUrlConstants.LOGIN)
    public String getLogin(Model model, @AuthenticationPrincipal AuthContext authContext) {
        if (authContext != null)
            return "redirect:/";

        model.addAttribute("formDto", new UserLoginForm());

        return "pages/login";
    }

    /**
     * only public
     */
    @PostMapping(PublicControllerUrlConstants.LOGIN)
    public String postLogin(@Valid @ModelAttribute("formDto") UserLoginForm userLoginForm,
            BindingResult bindingResult, @AuthenticationPrincipal AuthContext authContext,
            HttpServletResponse response) {
        if (authContext != null)
            return "redirect:/";

        if (bindingResult.hasErrors())
            return "pages/login";

        try {
            UserTokenResponseCookie userTokenDto = userApplicationService.login(userLoginForm);
            CookieUtil.setCookie(response, userTokenDto);

            return "redirect:/";
        } catch (IllegalArgumentException e) {
            bindingResult.reject("serverError", e.getMessage());
            return "pages/login";
        }
    }

    /**
     * only public
     */
    @GetMapping(PublicControllerUrlConstants.SIGNUP)
    public String getSignup(Model model, @AuthenticationPrincipal AuthContext authContext,
            @RequestParam(name = "uid", required = false) String uid) {
        if (authContext != null)
            return "redirect:/";

        if (uid != null) {
            OAuthSignupView oauthSignupView = authUserStore.getAndRemove(uid);
            if (oauthSignupView != null) {
                model.addAttribute("oauth", oauthSignupView);
            }
        }

        model.addAttribute("formDto", new UserSignupForm());

        return "pages/signup";
    }

    /**
     * only public
     */
    @PostMapping(PublicControllerUrlConstants.SIGNUP)
    public String postSignup(@Valid @ModelAttribute("formDto") UserSignupForm userSignupDto,
            BindingResult bindingResult, @AuthenticationPrincipal AuthContext authContext,
            HttpServletResponse response) {
        if (authContext != null)
            return "redirect:/";

        if (bindingResult.hasErrors())
            return "pages/signup";

        try {
            UserTokenResponseCookie userTokenDto = userApplicationService.signup(userSignupDto);
            CookieUtil.setCookie(response, userTokenDto);

            return "redirect:/";
        } catch (IllegalArgumentException e) {
            bindingResult.reject("serverError", e.getMessage());
            return "pages/signup";
        }
    }

    @GetMapping(PublicControllerUrlConstants.MAIN)
    public String getMain(Model model) {
        return "pages/main";
    }

    @GetMapping(PublicControllerUrlConstants.CHART)
    public String getChart(
            @RequestParam(name = "symbol", required = true, defaultValue = "btc") String symbol,
            @AuthenticationPrincipal AuthContext authContext, Model model) throws Exception {
        String upperCaseSymbol = symbol.toUpperCase();

        List<String> supportedSymbols = symbolVariableService.getSupportedSymbolNames();

        if (!supportedSymbols.contains(upperCaseSymbol)) {
            return "redirect:/";
        }

        // symbol에 해당하는 chart데이터
        List<PriceView> priceViewList =
                priceApplicationService.getInitialPriceOfSymbolName(upperCaseSymbol);

        // symbol에 해당하는 시장가 주문시 step size, 최대 주문 개수, 최소 주문 개수 구해오기.
        BinanceExchangeInfoResponse symbolInfo =
                collectorScheduleService.getExchangeInfo(upperCaseSymbol);

        UserTradeInfo userTradeInfo = authContext != null
                ? orderApplicationService.getSymbolInfo(symbol, authContext.getUserId())
                : null;

        model.addAttribute("supportedSymbols", supportedSymbols);
        model.addAttribute("prices", priceViewList);
        model.addAttribute("symbolInfo", symbolInfo);
        model.addAttribute("userTradeInfo", userTradeInfo);

        return "pages/chart";
    }
}
