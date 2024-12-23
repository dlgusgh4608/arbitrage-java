package main.arbitrage.infrastructure.exchange.upbit.dto.response;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import main.arbitrage.infrastructure.exchange.upbit.dto.enums.UpbitOrderEnums;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@ToString
public class UpbitGetOrderResponse {
    private String uuid;
    private String side;
    private String ordType;
    private Double price;
    private UpbitOrderEnums.State state;
    private String createdAt;
    private double volume;
    private double paidFee;
    private double executedVolume;
    private List<Trade> trades;

    @Getter
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    @AllArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Trade {
        private Double funds;
    }
}
