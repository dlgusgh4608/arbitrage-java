package main.arbitrage.infrastructure.exchange.upbit.priv.rest.dto.order;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.*;

import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class UpbitGetOrderResponseDto {
    private String uuid;
    private String side;
    private String ordType;
    private String price;
    private UpbitOrderEnum.State state;
    private String market;
    private String createdAt;
    private String volume;
    private String remainingVolume;
    private String reservedFee;
    private String remainingFee;
    private String paidFee;
    private String locked;
    private String executedVolume;
    private Integer tradesCount;
    private List<Trade> trades;
    private String timeInForce;
    private String identifier;

    @Builder
    private UpbitGetOrderResponseDto(
            String uuid,
            String side,
            String ordType,
            String price,
            State state,
            String market,
            String createdAt,
            String volume,
            String remainingVolume,
            String reservedFee,
            String remainingFee,
            String paidFee,
            String locked,
            String executedVolume,
            Integer tradesCount,
            List<Trade> trades, String timeInForce,
            String identifier
    ) {
        this.uuid = uuid;
        this.side = side;
        this.ordType = ordType;
        this.price = price;
        this.state = state;
        this.market = market;
        this.createdAt = createdAt;
        this.volume = volume;
        this.remainingVolume = remainingVolume;
        this.reservedFee = reservedFee;
        this.remainingFee = remainingFee;
        this.paidFee = paidFee;
        this.locked = locked;
        this.executedVolume = executedVolume;
        this.tradesCount = tradesCount;
        this.trades = trades;
        this.timeInForce = timeInForce;
        this.identifier = identifier;
    }

    @Getter
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class Trade {
        private String market;
        private String uuid;
        private String price;
        private String volume;
        private String funds;
        private String side;
        private String createdAt;
        private String trend;

        @Builder
        private Trade(
                String market,
                String uuid,
                String price,
                String volume,
                String funds,
                String side,
                String createdAt,
                String trend
        ) {
            this.market = market;
            this.uuid = uuid;
            this.price = price;
            this.volume = volume;
            this.funds = funds;
            this.side = side;
            this.createdAt = createdAt;
            this.trend = trend;
        }
    }
}
