package main.arbitrage.presentation.controller.pub.constant;

import java.util.List;

public final class PublicControllerUrlConstants {
  public static final String DEFAULT_URL = "/";
  public static final String MAIN = "";
  public static final String LOGIN = "login";
  public static final String SIGNUP = "signup";
  public static final String CHART = "chart";

  public static final List<String> PUBLIC_URLS =
      List.of(DEFAULT_URL + MAIN, DEFAULT_URL + LOGIN, DEFAULT_URL + SIGNUP, DEFAULT_URL + CHART);
}
