package main.arbitrage.presentation.dto.view;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class UserProfileView {
    private final String nickname;
    private final double exchangeRate;
    private final Double binanceBalance;
    private final Double upbitBalance;
}
