package main.arbitrage.application.user.dto;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class UserProfileDto {
    private String nickname;
    private double exchangeRate;
    private Double binanceBalance;
    private Double upbitBalance;
}