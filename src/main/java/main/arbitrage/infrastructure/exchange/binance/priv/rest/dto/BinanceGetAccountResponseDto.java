package main.arbitrage.infrastructure.exchange.binance.priv.rest.dto;

import lombok.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class BinanceGetAccountResponseDto {
    // 계정 별칭
    private String accountAlias;

    // 자산 종류 (예: BTC, ETH 등)
    private String asset;

    // 총 잔액
    private String balance;

    // 크로스 마진 계정의 지갑 잔액
    private String crossWalletBalance;

    // 크로스 마진 계정의 미실현 손익
    private String crossUnPnl;

    // 출금 가능한 잔액
    private String availableBalance;

    // 최대 출금 가능 금액
    private String maxWithdrawAmount;

    // 마진 거래 가능 여부
    private Boolean marginAvailable;

    // 마지막 업데이트 시간 (Unix 타임스탬프)
    private Long updateTime;
}