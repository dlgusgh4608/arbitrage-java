package main.arbitrage.infrastructure.exchange.upbit.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Builder;
import lombok.Getter;
import main.arbitrage.infrastructure.exchange.upbit.dto.enums.UpbitOrderEnums;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class UpbitPostOrderRequest {
    private final String market;

    private final UpbitOrderEnums.Side side;
    private final UpbitOrderEnums.OrdType ordType;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "0")
    private final Double price;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private final Double volume;
}