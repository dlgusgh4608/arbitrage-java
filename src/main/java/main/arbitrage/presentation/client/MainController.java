package main.arbitrage.presentation.client;

import lombok.RequiredArgsConstructor;
import main.arbitrage.auth.jwt.JwtUtil;
import main.arbitrage.auth.oauth.dto.OAuthDto;
import main.arbitrage.auth.oauth.store.OAuthStore;
import main.arbitrage.auth.security.CustomUserDetails;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/")
@RequiredArgsConstructor
public class MainController {
    private final JwtUtil jwtUtil;
    private final OAuthStore authStore;

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
            OAuthDto oauthDto = authStore.getAndRemove(uid);
            if (oauthDto != null) {
                model.addAttribute("oauth", oauthDto);
            }
        }

        return "pages/signup";
    }
}