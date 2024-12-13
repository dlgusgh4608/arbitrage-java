package main.arbitrage.presentation.restController.priv.constant;

import java.util.List;

public final class PrivateRestControllerUrlConstants {
    public static final String DEFAULT_URL = "/api";
    public static final String EDIT_NICKNAME = "/edit-nickname";

    public static final List<String> PRIVATE_URLS = List.of(
            DEFAULT_URL + EDIT_NICKNAME
    );
}