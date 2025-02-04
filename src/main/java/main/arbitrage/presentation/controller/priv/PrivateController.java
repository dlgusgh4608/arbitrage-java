package main.arbitrage.presentation.controller.priv;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import main.arbitrage.application.auto.service.AutoApplicationService;
import main.arbitrage.application.user.service.UserApplicationService;
import main.arbitrage.domain.symbol.service.SymbolVariableService;
import main.arbitrage.presentation.controller.priv.constant.PrivateControllerUrlConstants;
import main.arbitrage.presentation.dto.form.AutoTradingStrategyForm;
import main.arbitrage.presentation.dto.form.UserEnvForm;
import main.arbitrage.presentation.dto.view.UserProfileView;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping(PrivateControllerUrlConstants.DEFAULT_URL)
@RequiredArgsConstructor
public class PrivateController {
  private final AutoApplicationService autoApplicationService;
  private final SymbolVariableService symbolVariableService;
  private final UserApplicationService userApplicationService;

  @Value("${ip-address}")
  private String IP_ADDRESS;

  @GetMapping(PrivateControllerUrlConstants.USER_ENV_REGISTER)
  public String envRegisterGet(Model model) {
    UserEnvForm userEnvForm = new UserEnvForm();

    model.addAttribute("formDto", userEnvForm);
    model.addAttribute("ipAddress", IP_ADDRESS);

    return "pages/envRegister";
  }

  @PostMapping(PrivateControllerUrlConstants.USER_ENV_REGISTER)
  public String envRegisterPost(
      @Valid @ModelAttribute("formDto") UserEnvForm userEnvForm, BindingResult bindingResult) {
    if (bindingResult.hasErrors()) return "pages/envRegister";

    try {
      userApplicationService.registerUserEnv(userEnvForm);

      return "redirect:/";
    } catch (Exception e) {
      bindingResult.reject("serverError", e.getMessage());
      return "pages/envRegister";
    }
  }

  @GetMapping(PrivateControllerUrlConstants.USER_PROFILE)
  public String profileGet(Model model) {
    UserProfileView userProfileView = userApplicationService.getUserProfile();
    model.addAttribute("profile", userProfileView);

    return "pages/profile";
  }

  @GetMapping(PrivateControllerUrlConstants.ORDER_HISTORY)
  public String getOrderHistory(Model model) {
    model.addAttribute("orders", userApplicationService.getBuyOrderOfPage(0));

    return "pages/orderHistory";
  }

  @GetMapping(PrivateControllerUrlConstants.ORDER_SETTING)
  public String getOrderSetting(Model model) {
    AutoTradingStrategyForm autoTradingStrategyForm =
        autoApplicationService.getAutoTradingStrategyForm();

    model.addAttribute("symbols", symbolVariableService.getSupportedSymbolNames());
    if (autoTradingStrategyForm == null) {
      model.addAttribute("formDto", new AutoTradingStrategyForm());
    } else {
      model.addAttribute("formDto", autoTradingStrategyForm);
    }

    return "pages/setting";
  }

  @PostMapping(PrivateControllerUrlConstants.ORDER_SETTING)
  public String postOrderSetting(
      Model model,
      @Valid @ModelAttribute("formDto") AutoTradingStrategyForm autoTradingStrategyForm,
      BindingResult bindingResult) {

    model.addAttribute("symbols", symbolVariableService.getSupportedSymbolNames());

    if (bindingResult.hasErrors()) return "pages/setting";

    try {
      model.addAttribute(
          "formDto", autoApplicationService.updateAutoTradingSetting(autoTradingStrategyForm));

      return "pages/setting";
    } catch (Exception e) {
      bindingResult.reject("serverError", e.getMessage());
      return "pages/setting";
    }
  }
}
