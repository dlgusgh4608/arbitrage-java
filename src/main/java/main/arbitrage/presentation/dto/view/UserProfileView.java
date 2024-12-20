package main.arbitrage.presentation.dto.view;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class UserProfileView {
    private String nickname;
    private double exchangeRate;
    private Double binanceBalance;
    private Double upbitBalance;
}
