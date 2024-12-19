package main.arbitrage.infrastructure.exchange.upbit.priv.rest.dto.order;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.*;

import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@ToString
public class UpbitGetOrderResponseDto {
    private String uuid;
    private String side;
    private String ordType;
    private Double price;
    private UpbitOrderEnum.State state;
    private String createdAt;
    private double volume;
    private double paidFee;
    // private String locked;
    private double executedVolume;
    private List<Trade> trades;
    // private String market;
    // private String remainingVolume;
    // private String reservedFee;
    // private String remainingFee;
    // private Integer tradesCount;
    // private String timeInForce;
    // private String identifier;

    @Builder
    private UpbitGetOrderResponseDto(
            String uuid,
            String side,
            String ordType,
            String price,
            UpbitOrderEnum.State state,
            String createdAt,
            String volume,
            String paidFee,
            String executedVolume,
            List<Trade> trades
            // String market,
            // String remainingVolume,
            // String reservedFee,
            // String remainingFee,
            // String locked,
            // Integer tradesCount,
            // String timeInForce
            // String identifier
    ) {
        this.uuid = uuid;
        this.side = side;
        this.ordType = ordType;
        this.price = Double.valueOf(price);
        this.state = state;
        this.createdAt = createdAt;
        this.volume = Double.parseDouble(volume);
        this.paidFee = Double.parseDouble(paidFee);
        this.executedVolume = Double.parseDouble(executedVolume);
        this.trades = trades;
        // this.market = market;
        // this.remainingVolume = remainingVolume;
        // this.reservedFee = reservedFee;
        // this.remainingFee = remainingFee;
        // this.locked = locked;
        // this.tradesCount = tradesCount;
        // this.timeInForce = timeInForce;
        // this.identifier = identifier;
    }

    @Getter
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Trade {
        private Double funds;
        // private String market;
        // private String uuid;
        // private String price;
        // private String volume;
        // private String side;
        // private String createdAt;
        // private String trend;

        @Builder
        private Trade(
            String funds
            // String market,
            // String uuid,
            // String price,
            // String volume,
            // String side,
            // String createdAt,
            // String trend
        ) {
            this.funds = Double.valueOf(funds);
            // this.market = market;
            // this.uuid = uuid;
            // this.price = price;
            // this.volume = volume;
            // this.side = side;
            // this.createdAt = createdAt;
            // this.trend = trend;
        }
    }
}
