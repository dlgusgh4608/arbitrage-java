package main.arbitrage.infrastructure.exchange.upbit.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class UpbitGetAccountResponse {
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
