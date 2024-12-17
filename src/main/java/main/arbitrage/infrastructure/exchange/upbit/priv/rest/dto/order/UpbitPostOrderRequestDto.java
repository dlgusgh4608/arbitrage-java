package main.arbitrage.infrastructure.exchange.upbit.priv.rest.dto.order;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class UpbitPostOrderRequestDto {
    private String market;

    private Side side;
    private OrdType ordType;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "0")
    private Double price;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Double volume;
}