package main.arbitrage.infrastructure.exchange.binance.pub.rest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import main.arbitrage.domain.symbol.service.SymbolVariableService;
import main.arbitrage.infrastructure.exchange.binance.dto.response.BinanceExchangeInfoResponse;
import main.arbitrage.infrastructure.exchange.binance.exception.BinanceRestException;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

@Service
public class BinancePublicRestService extends BaseBinancePublicRestService {

    public BinancePublicRestService(OkHttpClient okHttpClient, ObjectMapper objectMapper,
            SymbolVariableService symbolVariableService) {
        super(okHttpClient, objectMapper, symbolVariableService);
    }

    public Map<String, BinanceExchangeInfoResponse> getExchangeInfo() {
        try {
            String url = DEFAULT_URL + "/v1/exchangeInfo";

            Request request = new Request.Builder().url(url)
                    .addHeader("Content-Type", "application/json").get().build();

            Response response = okHttpClient.newCall(request).execute();

            String responseBody = response.body().string();

            if (!response.isSuccessful()) {
                validateResponse(responseBody);
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
        } catch (Exception e) {
            throw new BinanceRestException("(바이낸스) 알 수 없는 에러가 발생했습니다.", "UNKNOWN_ERROR");
        }

    }
}
