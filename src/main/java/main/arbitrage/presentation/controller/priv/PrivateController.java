package main.arbitrage.presentation.controller.priv;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import main.arbitrage.application.user.service.UserApplicationService;
import main.arbitrage.presentation.controller.priv.constant.PrivateControllerUrlConstants;
import main.arbitrage.presentation.dto.form.UserEnvForm;
import main.arbitrage.presentation.dto.view.UserProfileView;
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
  private final UserApplicationService userApplicationService;

  @GetMapping(PrivateControllerUrlConstants.USER_ENV_REGISTER)
  public String envRegisterGet(Model model) {
    UserEnvForm userEnvForm = new UserEnvForm();

    model.addAttribute("formDto", userEnvForm);

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
}
