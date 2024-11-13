package main.arbitrage.global.constant;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class SupportedSymbol {
    public static final String BTC = "btc";
    public static final String ETH = "eth";

    private static final List<String> SUPPORTED_SYMBOLS = Collections.unmodifiableList(
            Arrays.asList(BTC, ETH)
    );

    public static List<String> getApplySymbols() {
        return SUPPORTED_SYMBOLS;
    }
}