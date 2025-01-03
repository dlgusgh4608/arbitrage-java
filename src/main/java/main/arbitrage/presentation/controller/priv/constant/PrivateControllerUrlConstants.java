package main.arbitrage.presentation.controller.priv.constant;

import java.util.List;

public final class PrivateControllerUrlConstants {
  public static final String DEFAULT_URL = "/";
  public static final String USER_ENV_REGISTER = "user/env-register";
  public static final String USER_PROFILE = "user/profile";

  public static final List<String> PUBLIC_URLS =
      List.of(DEFAULT_URL + USER_ENV_REGISTER, DEFAULT_URL + USER_PROFILE);
}
