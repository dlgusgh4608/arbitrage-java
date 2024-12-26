package main.arbitrage.presentation.dto.view;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class UserWalletInfoView {
    private final Double usdt;
    private final Double krw;
}
