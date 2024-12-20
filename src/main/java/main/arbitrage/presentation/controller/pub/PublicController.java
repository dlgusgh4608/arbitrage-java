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
import main.arbitrage.application.price.dto.PriceDto;
import main.arbitrage.application.price.service.PriceApplicationService;
import main.arbitrage.application.user.service.UserApplicationService;
import main.arbitrage.domain.oauthUser.dto.OAuthUserDto;
import main.arbitrage.domain.oauthUser.store.OAuthUserStore;
import main.arbitrage.domain.symbol.service.SymbolVariableService;
import main.arbitrage.domain.user.dto.UserLoginDto;
import main.arbitrage.domain.user.dto.UserSignupDto;
import main.arbitrage.domain.user.dto.UserTokenDto;
import main.arbitrage.domain.user.entity.User;
import main.arbitrage.global.util.cookie.CookieUtil;
import main.arbitrage.presentation.controller.pub.constant.PublicControllerUrlConstants;

@Controller
@RequestMapping(PublicControllerUrlConstants.DEFAULT_URL)
@RequiredArgsConstructor
public class PublicController {
    private final UserApplicationService userApplicationService;
    private final SymbolVariableService symbolVariableService;
    private final OAuthUserStore authUserStore;
    private final PriceApplicationService priceApplicationService;

    /**
     * only public
     */
    @GetMapping(PublicControllerUrlConstants.LOGIN)
    public String getLogin(Model model, @AuthenticationPrincipal User userDetails) {
        if (userDetails != null)
            return "redirect:/";

        UserLoginDto userLoginDto = new UserLoginDto();
        model.addAttribute("formDto", userLoginDto);

        return "pages/login";
    }

    /**
     * only public
     */
    @PostMapping(PublicControllerUrlConstants.LOGIN)
    public String postLogin(@Valid @ModelAttribute("formDto") UserLoginDto userLoginDto,
            BindingResult bindingResult, @AuthenticationPrincipal User user,
            HttpServletResponse response) {
        if (user != null)
            return "redirect:/";

        if (bindingResult.hasErrors())
            return "pages/login";

        try {
            UserTokenDto userTokenDto = userApplicationService.login(userLoginDto);
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
    public String getSignup(Model model, @AuthenticationPrincipal User user,
            @RequestParam(name = "uid", required = false) String uid) {
        if (user != null)
            return "redirect:/";

        if (uid != null) {
            OAuthUserDto oauthUserDto = authUserStore.getAndRemove(uid);
            if (oauthUserDto != null) {
                model.addAttribute("oauth", oauthUserDto);
            }
        }

        model.addAttribute("formDto", new UserSignupDto());

        return "pages/signup";
    }

    /**
     * only public
     */
    @PostMapping(PublicControllerUrlConstants.SIGNUP)
    public String postSignup(@Valid @ModelAttribute("formDto") UserSignupDto userSignupDto,
            BindingResult bindingResult, @AuthenticationPrincipal User user,
            HttpServletResponse response) {
        if (user != null)
            return "redirect:/";

        if (bindingResult.hasErrors())
            return "pages/signup";

        try {
            UserTokenDto userTokenDto = userApplicationService.signup(userSignupDto);
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
            Model model) {
        List<String> supportedSymbols = symbolVariableService.getSupportedSymbols().stream()
                .map(s -> s.getName().toUpperCase()).toList();

        if (!supportedSymbols.contains(symbol.toUpperCase())) {
            return "redirect:/";
        }

        List<PriceDto> priceDTOs =
                priceApplicationService.getInitialPriceOfSymbolName(symbol.toLowerCase());


        model.addAttribute("supportedSymbols", supportedSymbols);
        model.addAttribute("prices", priceDTOs);

        return "pages/chart";
    }
}
