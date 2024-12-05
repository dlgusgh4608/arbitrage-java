package main.arbitrage.presentation.client;

import java.util.List;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import main.arbitrage.domain.symbol.entity.Symbol;
import main.arbitrage.domain.symbol.service.SymbolVariableService;
import main.arbitrage.domain.userEnv.dto.UserEnvFormDto;
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
@RequestMapping("/")
@RequiredArgsConstructor
public class MainController {
    private final SymbolVariableService symbolVariableService;
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

    @GetMapping("/user-env/register")
    public String envRegisterGet(Model model) {
        UserEnvFormDto userEnvFormDto = new UserEnvFormDto();

        model.addAttribute("formDto", userEnvFormDto);

        return "pages/env-register";
    }

    @PostMapping("/user-env/register")
    public String envRegisterPost(@Valid @ModelAttribute("formDto") UserEnvFormDto dto, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "pages/env-register";
        }

        return "redirect:/";
    }
}