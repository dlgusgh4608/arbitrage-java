package main.arbitrage.presentation.client;

import lombok.RequiredArgsConstructor;
import main.arbitrage.auth.jwt.JwtUtil;
import main.arbitrage.domain.oauthUser.dto.OAuthUserDto;
import main.arbitrage.domain.oauthUser.store.OAuthUserStore;
import main.arbitrage.auth.security.CustomUserDetails;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/")
@RequiredArgsConstructor
public class MainController {
    private final JwtUtil jwtUtil;
    private final OAuthUserStore authUserStore;

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
    public String chart(Model model) {
        List<CandleData> data = new ArrayList<>();
        LocalDateTime startDate = LocalDateTime.now().minusDays(30);
        double basePrice = 3500.0;

        for (int i = 0; i < 900; i++) {
            double open = basePrice + (Math.random() - 0.5) * 50;
            double close = open + (Math.random() - 0.5) * 40;
            double high = Math.max(open, close) + Math.random() * 20;
            double low = Math.min(open, close) - Math.random() * 20;

            data.add(new CandleData(
                    startDate.plusDays(i).toString(),
                    open,
                    high,
                    low,
                    close
            ));

            basePrice = close;
        }

        model.addAttribute("chartData", data);

        return "pages/chart";
    }
}