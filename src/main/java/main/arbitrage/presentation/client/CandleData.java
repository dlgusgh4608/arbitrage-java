package main.arbitrage.presentation.client;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
class CandleData {
    private String date;
    private double open;
    private double high;
    private double low;
    private double close;
}