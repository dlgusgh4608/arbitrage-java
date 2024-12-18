package main.arbitrage.presentation.controller.priv;

import jakarta.validation.Valid;
import main.arbitrage.application.user.dto.UserProfileDto;
import main.arbitrage.application.user.service.UserApplicationService;
import main.arbitrage.domain.userEnv.dto.UserEnvDto;
import main.arbitrage.presentation.controller.priv.constant.PrivateControllerUrlConstants;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;


@Controller
@RequestMapping(PrivateControllerUrlConstants.DEFAULT_URL)
@RequiredArgsConstructor
public class PrivateController {
    private final UserApplicationService userApplicationService;

    @GetMapping(PrivateControllerUrlConstants.USER_ENV_REGISTER)
    public String envRegisterGet(Model model) {
        UserEnvDto userEnvDto = new UserEnvDto();

        model.addAttribute("formDto", userEnvDto);

        return "pages/envRegister";
    }

    @PostMapping(PrivateControllerUrlConstants.USER_ENV_REGISTER)
    public String envRegisterPost(
            @Valid @ModelAttribute("formDto") UserEnvDto userEnvDto,
            BindingResult bindingResult
    ) {
        if (bindingResult.hasErrors()) return "pages/envRegister";

        try {
            userApplicationService.registerUserEnv(userEnvDto);

            return "redirect:/";
        } catch (Exception e) {
            bindingResult.reject("serverError", e.getMessage());
            return "pages/envRegister";
        }
    }

    @GetMapping(PrivateControllerUrlConstants.USER_PROFILE)
    public String profileGet(Model model) throws Exception {
        UserProfileDto userProfileDto = userApplicationService.getUserProfile();
        model.addAttribute("profile", userProfileDto);

        return "pages/profile";
    }
}