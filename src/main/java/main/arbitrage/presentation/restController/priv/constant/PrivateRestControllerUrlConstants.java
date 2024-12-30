package main.arbitrage.presentation.restController.priv.constant;

import java.util.List;

public final class PrivateRestControllerUrlConstants {
    public static final String DEFAULT_URL = "/api";
    public static final String EDIT_NICKNAME = "/edit-nickname";
    public static final String BUY_ORDER = "/buy-order";
    public static final String UPDATE_LEVERAGE = "/update-leverage";

    public static final List<String> PRIVATE_URLS = List.of(DEFAULT_URL + EDIT_NICKNAME,
            DEFAULT_URL + BUY_ORDER, DEFAULT_URL + UPDATE_LEVERAGE);
}
