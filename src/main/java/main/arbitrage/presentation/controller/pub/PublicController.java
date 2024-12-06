package main.arbitrage.presentation.controller.pub;

import java.util.List;
import java.util.stream.Collectors;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import main.arbitrage.application.user.service.UserApplicationService;
import main.arbitrage.domain.symbol.entity.Symbol;
import main.arbitrage.domain.symbol.service.SymbolVariableService;
import main.arbitrage.domain.user.dto.UserLoginDto;
import main.arbitrage.domain.user.dto.UserSignupDto;
import main.arbitrage.domain.user.dto.UserTokenDto;
import main.arbitrage.global.util.cookie.CookieUtil;
import main.arbitrage.presentation.controller.pub.constant.PublicControllerUrlConstants;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;
import main.arbitrage.application.collector.service.CollectorService;
import main.arbitrage.auth.security.CustomUserDetails;
import main.arbitrage.domain.oauthUser.dto.OAuthUserDto;
import main.arbitrage.domain.oauthUser.store.OAuthUserStore;
import main.arbitrage.domain.price.dto.PriceDto;
import main.arbitrage.domain.price.entity.Price;

@Controller
@RequestMapping(PublicControllerUrlConstants.DEFAULT_URL)
@RequiredArgsConstructor
public class PublicController {
    private final UserApplicationService userApplicationService;
    private final SymbolVariableService symbolVariableService;
    private final OAuthUserStore authUserStore;
    private final CollectorService collectorService;

    /**
     * only public
     */
    @GetMapping(PublicControllerUrlConstants.LOGIN)
    public String getLogin(
            Model model,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        if (userDetails != null) return "redirect:/";

        UserLoginDto userLoginDto = new UserLoginDto();
        model.addAttribute("formDto", userLoginDto);

        return "pages/login";
    }

    /**
     * only public
     */
    @PostMapping(PublicControllerUrlConstants.LOGIN)
    public String postLogin(
            @Valid @ModelAttribute("formDto") UserLoginDto userLoginDto,
            BindingResult bindingResult,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            HttpServletResponse response
    ) {
        if (userDetails != null) return "redirect:/";

        if (bindingResult.hasErrors()) return "pages/login";

        try {
            UserTokenDto userTokenDto = userApplicationService.login(userLoginDto);
            setCookies(userTokenDto, response);

            return "redirect:/";
        } catch (IllegalArgumentException e) {
            bindingResult.reject("loginError", e.getMessage());
            return "pages/login";
        }
    }

    /**
     * only public
     */
    @GetMapping(PublicControllerUrlConstants.SIGNUP)
    public String getSignup(
            Model model,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(name = "uid", required = false) String uid
    ) {
        if (userDetails != null) return "redirect:/";

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
    public String postSignup(
            @Valid @ModelAttribute("formDto") UserSignupDto userSignupDto,
            BindingResult bindingResult,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            HttpServletResponse response
    ) {
        if (userDetails != null) return "redirect:/";

        if (bindingResult.hasErrors()) return "pages/signup";

        try {
            UserTokenDto userTokenDto = userApplicationService.signup(userSignupDto);
            setCookies(userTokenDto, response);

            return "redirect:/";
        } catch (IllegalArgumentException e) {
            bindingResult.reject("signupError", e.getMessage());
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
            Model model
    ) {
        List<String> supportedSymbolNames = symbolVariableService.getSupportedSymbols()
                .stream()
                .map(Symbol::getName)
                .toList();

        if (!supportedSymbolNames.contains(symbol.toLowerCase())) {
            return "redirect:/";
        }

        List<Price> prices = collectorService.getInitialPriceOfSymbolName(symbol);
        List<PriceDto> priceDTOs = prices.stream()
                .map(PriceDto::from)
                .collect(Collectors.toList());


        model.addAttribute("prices", priceDTOs);

        return "pages/chart";
    }


    private void setCookies(UserTokenDto userTokenDto, HttpServletResponse response) {
        CookieUtil.addCookie(
                response,
                "refreshToken",
                userTokenDto.getRefreshToken(),
                userTokenDto.getRefreshTokenTTL().intValue(),
                true
        );
        CookieUtil.addCookie(
                response,
                "accessToken",
                userTokenDto.getAccessToken(),
                -1,
                true
        );
    }
}