package main.arbitrage.global.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class ControllerDefaultModel {

  @ModelAttribute("uri")
  public String uri(HttpServletRequest request) {
    return request.getRequestURI();
  }
}
