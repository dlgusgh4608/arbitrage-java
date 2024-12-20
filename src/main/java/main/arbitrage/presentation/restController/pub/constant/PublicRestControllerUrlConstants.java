package main.arbitrage.presentation.restController.pub.constant;

import java.util.List;

public final class PublicRestControllerUrlConstants {
    public static final String DEFAULT_URL = "/api";
    public static final String SEND_EMAIL = "/send-email";
    public static final String CHECK_CODE = "/check-code";

    public static final List<String> PUBLIC_URLS =
            List.of(DEFAULT_URL + SEND_EMAIL, DEFAULT_URL + CHECK_CODE);
}
