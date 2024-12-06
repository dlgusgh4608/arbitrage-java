package main.arbitrage.presentation.controller.priv;

import jakarta.validation.Valid;
import main.arbitrage.domain.userEnv.dto.UserEnvFormDto;
import main.arbitrage.presentation.controller.priv.constant.PrivateUrlConstants;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;


@Controller
@RequestMapping(PrivateUrlConstants.DEFAULT_URL)
@RequiredArgsConstructor
public class PrivateController {
    @GetMapping(PrivateUrlConstants.USER_ENV_REGISTER)
    public String envRegisterGet(Model model) {
        UserEnvFormDto userEnvFormDto = new UserEnvFormDto();

        model.addAttribute("formDto", userEnvFormDto);

        return "pages/envRegister";
    }

    @PostMapping(PrivateUrlConstants.USER_ENV_REGISTER)
    public String envRegisterPost(
            @Valid @ModelAttribute("formDto") UserEnvFormDto userEnvFormDto,
            BindingResult bindingResult
    ) {
        if (bindingResult.hasErrors()) return "pages/envRegister";

        return "redirect:/";
    }
}