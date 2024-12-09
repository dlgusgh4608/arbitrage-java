package main.arbitrage.infrastructure.upbit.priv.rest.dto;

import lombok.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class UpbitGetAccountResponseDto {
    // 화폐를 의미하는 영문 대문자 코드 example: "KRW"
    private String currency;

    // 주문가능 금액/수량 example: "10000.123"
    private String balance;

    // 주문 중 묶여있는 금액/수량 example: "500.0"
    private String locked;

    // 매수평균가 example: "35000.0"
    private String avgBuyPrice;

    // 매수평균가 수정 여부 example: "true"
    private Boolean avgBuyPriceModified;

    // 평단가 기준 화폐 example: "KRW"
    private String unitCurrency;
}