package main.arbitrage.presentation.client;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import lombok.RequiredArgsConstructor;
import main.arbitrage.application.collector.service.CollectorService;
import main.arbitrage.auth.security.CustomUserDetails;
import main.arbitrage.domain.oauthUser.dto.OAuthUserDto;
import main.arbitrage.domain.oauthUser.store.OAuthUserStore;
import main.arbitrage.domain.price.dto.PriceDto;
import main.arbitrage.domain.price.entity.Price;
import main.arbitrage.global.constant.SupportedSymbol;

@Controller
@RequestMapping("/")
@RequiredArgsConstructor
public class MainController {
    private final OAuthUserStore authUserStore;
    private final CollectorService collectorService;

    @GetMapping("/")
    public String mainPage(Model model) {
        return "pages/main";
    }

    @GetMapping("/login")
    public String login(Model model, @AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails != null) return "redirect:/";

        return "pages/login";
    }

    @GetMapping("/signup")
    public String signup(
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

        return "pages/signup";
    }

    @GetMapping("/chart")
    public String chart(
            @RequestParam(name = "symbol", required = true, defaultValue = "btc") String symbol,
            Model model
    ) {
        if (!SupportedSymbol.getApplySymbols().contains(symbol.toLowerCase())) {
            return "redirect:/";
        }

        List<Price> prices = collectorService.getInitialPriceOfSymbol(symbol);
        List<PriceDto> priceDTOs = prices.stream()
                .map(PriceDto::from)
                .collect(Collectors.toList());

        model.addAttribute("prices", priceDTOs);

        return "pages/chart";
    }
}