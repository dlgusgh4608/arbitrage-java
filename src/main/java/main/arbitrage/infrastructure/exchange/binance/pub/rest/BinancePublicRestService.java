package main.arbitrage.infrastructure.exchange.binance.pub.rest;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import main.arbitrage.domain.symbol.service.SymbolVariableService;
import main.arbitrage.infrastructure.exchange.binance.dto.response.BinanceExchangeInfoResponse;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

@Service
@RequiredArgsConstructor
public class BinancePublicRestService {
    private final ObjectMapper objectMapper;
    private final OkHttpClient okHttpClient;
    private final SymbolVariableService symbolVariableService;
    private final String DEFAULT_URL = "https://fapi.binance.com/fapi";

    public Map<String, BinanceExchangeInfoResponse> getExchangeInfo() throws IOException {
        String url = DEFAULT_URL + "/v1/exchangeInfo";

        Request request = new Request.Builder().url(url)
                .addHeader("Content-Type", "application/json").get().build();

        Response response = okHttpClient.newCall(request).execute();

        String responseBody = response.body().string();

        if (!response.isSuccessful()) {
            return null;
        }

        JsonNode json = objectMapper.readTree(responseBody);

        JsonNode symbols = json.get("symbols");

        List<String> supportedSymbolNames = symbolVariableService.getSupportedSymbolNames();

        Map<String, BinanceExchangeInfoResponse> exchangeHashMap = new HashMap<>();

        String CURRENT_FILTER_TYPE = "MARKET_LOT_SIZE";

        for (JsonNode symbol : symbols) {
            String symbolName = symbol.get("baseAsset").asText();
            String quoteAsset = symbol.get("quoteAsset").asText();

            if (supportedSymbolNames.contains(symbolName) && "USDT".equals(quoteAsset)) {
                JsonNode filters = symbol.get("filters");

                for (JsonNode filter : filters) {
                    if (CURRENT_FILTER_TYPE.equals(filter.get("filterType").asText())) {
                        exchangeHashMap.put(symbolName, objectMapper.treeToValue(filter,
                                BinanceExchangeInfoResponse.class));
                    }
                }
            }
        }

        return exchangeHashMap;
    }
}
