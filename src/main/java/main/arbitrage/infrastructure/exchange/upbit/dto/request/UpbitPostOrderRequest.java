package main.arbitrage.infrastructure.exchange.upbit.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import main.arbitrage.infrastructure.exchange.upbit.dto.enums.UpbitOrderEnums;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class UpbitPostOrderRequest {
    private String market;

    private UpbitOrderEnums.Side side;
    private UpbitOrderEnums.OrdType ordType;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "0")
    private Double price;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Double volume;
}
