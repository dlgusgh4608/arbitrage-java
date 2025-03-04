package main.arbitrage.presentation.controller.pub;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import main.arbitrage.application.collector.service.CollectorScheduleService;
import main.arbitrage.application.order.service.OrderApplicationService;
import main.arbitrage.application.user.service.UserApplicationService;
import main.arbitrage.auth.dto.AuthContext;
import main.arbitrage.domain.oauthUser.store.OAuthUserStore;
import main.arbitrage.domain.symbol.service.SymbolVariableService;
import main.arbitrage.global.util.cookie.CookieUtil;
import main.arbitrage.infrastructure.binance.dto.response.BinanceExchangeInfoResponse;
import main.arbitrage.presentation.controller.pub.constant.PublicControllerUrlConstants;
import main.arbitrage.presentation.dto.form.UserLoginForm;
import main.arbitrage.presentation.dto.form.UserSignupForm;
import main.arbitrage.presentation.dto.message.MessageModelSetFactory;
import main.arbitrage.presentation.dto.response.UserTokenResponseCookie;
import main.arbitrage.presentation.dto.view.OAuthSignupView;
import main.arbitrage.presentation.dto.view.UserTradeInfo;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping(PublicControllerUrlConstants.DEFAULT_URL)
@RequiredArgsConstructor
public class PublicController {
  private final UserApplicationService userApplicationService;
  private final SymbolVariableService symbolVariableService;
  private final OAuthUserStore authUserStore;
  private final CollectorScheduleService collectorScheduleService;
  private final OrderApplicationService orderApplicationService;
  private final MessageModelSetFactory modelSetFactory;

  /** only public */
  @GetMapping(PublicControllerUrlConstants.LOGIN)
  public String getLogin(
      Model model,
      RedirectAttributes redirectAttributes,
      @AuthenticationPrincipal AuthContext authContext) {
    if (authContext != null) {
      modelSetFactory.createMessage(redirectAttributes, false, "로그아웃을 먼저 진행해 주세요.");
      return "redirect:/";
    }

    model.addAttribute("formDto", new UserLoginForm());

    return "pages/login";
  }

  /** only public */
  @PostMapping(PublicControllerUrlConstants.LOGIN)
  public String postLogin(
      RedirectAttributes redirectAttributes,
      @Valid @ModelAttribute("formDto") UserLoginForm userLoginForm,
      BindingResult bindingResult,
      @AuthenticationPrincipal AuthContext authContext,
      HttpServletResponse response) {
    if (authContext != null) {
      modelSetFactory.createMessage(redirectAttributes, false, "로그아웃을 먼저 진행해 주세요.");
      return "redirect:/";
    }

    if (bindingResult.hasErrors()) return "pages/login";

    try {
      UserTokenResponseCookie userTokenDto = userApplicationService.login(userLoginForm);
      CookieUtil.setCookie(response, userTokenDto);

      modelSetFactory.createMessage(redirectAttributes, true, "로그인 완료");
      return "redirect:/";
    } catch (Exception e) {
      bindingResult.reject("serverError", e.getMessage());
      return "pages/login";
    }
  }

  @GetMapping(PublicControllerUrlConstants.FROM_OAUTH_TO_MAIN)
  public String fromOAuthToMain(RedirectAttributes redirectAttributes) {
    modelSetFactory.createMessage(redirectAttributes, true, "로그인 완료");
    return "redirect:/";
  }

  /** only public */
  @GetMapping(PublicControllerUrlConstants.SIGNUP)
  public String getSignup(
      Model model,
      RedirectAttributes redirectAttributes,
      @AuthenticationPrincipal AuthContext authContext,
      @RequestParam(name = "uid", required = false) String uid) {
    if (authContext != null) {
      modelSetFactory.createMessage(redirectAttributes, false, "로그아웃을 먼저 진행해 주세요.");
      return "redirect:/";
    }

    if (uid != null) {
      OAuthSignupView oauthSignupView = authUserStore.getAndRemove(uid);
      if (oauthSignupView != null) {
        model.addAttribute("oauth", oauthSignupView);
      }
    }

    model.addAttribute("formDto", new UserSignupForm());

    return "pages/signup";
  }

  /** only public */
  @PostMapping(PublicControllerUrlConstants.SIGNUP)
  public String postSignup(
      Model model,
      RedirectAttributes redirectAttributes,
      @Valid @ModelAttribute("formDto") UserSignupForm userSignupDto,
      BindingResult bindingResult,
      @AuthenticationPrincipal AuthContext authContext,
      HttpServletResponse response) {
    if (authContext != null) {
      modelSetFactory.createMessage(redirectAttributes, false, "로그아웃을 먼저 진행해 주세요.");
      return "redirect:/";
    }

    if (bindingResult.hasErrors()) {
      if (userSignupDto.getAccessToken().isEmpty() == false) {
        model.addAttribute(
            "oauth",
            OAuthSignupView.builder()
                .accessToken(userSignupDto.getAccessToken())
                .email(userSignupDto.getEmail())
                .provider(userSignupDto.getProvider())
                .providerId(userSignupDto.getProviderId())
                .build());
      }
      return "pages/signup";
    }

    try {
      UserTokenResponseCookie userTokenDto = userApplicationService.signup(userSignupDto);
      CookieUtil.setCookie(response, userTokenDto);

      modelSetFactory.createMessage(redirectAttributes, true, "회원가입 완료");
      return "redirect:/";
    } catch (Exception e) {
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
      Model model,
      RedirectAttributes redirectAttributes,
      @RequestParam(name = "symbol", required = true, defaultValue = "btc") String symbol,
      @AuthenticationPrincipal AuthContext authContext) {
    String upperCaseSymbol = symbol.toUpperCase();

    List<String> supportedSymbols = symbolVariableService.getSupportedSymbolNames();

    if (!supportedSymbols.contains(upperCaseSymbol)) {
      modelSetFactory.createMessage(
          redirectAttributes, false, String.format("[ %s ] 지원하지 않는 심볼입니다.", symbol));
      return "redirect:/";
    }

    // symbol에 해당하는 시장가 주문시 step size, 최대 주문 개수, 최소 주문 개수 구해오기.
    BinanceExchangeInfoResponse symbolInfo =
        collectorScheduleService.getExchangeInfo(upperCaseSymbol);

    UserTradeInfo userTradeInfo =
        authContext != null
            ? orderApplicationService.getTradeInfo(upperCaseSymbol, authContext.getUserId())
            : null;

    if (userTradeInfo != null) model.addAttribute("userTradeInfo", userTradeInfo);
    else model.addAttribute("userTradeInfo", Collections.emptyMap());

    if (symbolInfo != null) model.addAttribute("symbolInfo", symbolInfo);
    else model.addAttribute("symbolInfo", Collections.emptyMap());

    model.addAttribute("supportedSymbols", supportedSymbols);

    return "pages/chart/index";
  }
}
