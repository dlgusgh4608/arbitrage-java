package main.arbitrage.infrastructure.exchange.upbit.priv.rest.dto.order;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class UpbitPostOrderRequestDto {
    private String market;

    private UpbitOrderEnum.Side side;
    private UpbitOrderEnum.OrdType ordType;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "0")
    private Double price;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Double volume;
}
