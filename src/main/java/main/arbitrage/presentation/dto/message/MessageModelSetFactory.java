package main.arbitrage.presentation.dto.message;

import org.springframework.stereotype.Component;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Component
public class MessageModelSetFactory {
  public void createMessage(Model model, boolean isSuccess, String message) {
    model.addAttribute(
        "toast", MessageResponse.builder().isSuccess(isSuccess).message(message).build());
  }

  public void createMessage(
      RedirectAttributes redirectAttributes, boolean isSuccess, String message) {
    redirectAttributes.addFlashAttribute(
        "toast", MessageResponse.builder().isSuccess(isSuccess).message(message).build());
  }
}
