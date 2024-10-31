package main.arbitrage.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import main.arbitrage.infrastructure.event.EventEmitter;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Slf4j
@Service
@EnableScheduling
public class UsdToKrw {
    private static final String GOOGLE_FINANCE_USD_TO_KRW_URL = "https://www.google.com/finance/quote/USD-KRW";
    private static final String CURRENT_SELECTOR = "div.YMlKec.fxKbKc"; // 환율 셀렉터 -> 나중에 구글이 수정하면 바꿔야함.
    private final ObjectMapper objectMapper;
    private final EventEmitter emitter;

    double rate;

    public UsdToKrw(ObjectMapper objectMapper, EventEmitter emitter) {
        this.objectMapper = objectMapper;
        this.emitter = emitter;
    }

    private double getUsdToKrw() {
        try {
            Document doc = Jsoup.connect(GOOGLE_FINANCE_USD_TO_KRW_URL).get();

            String rateToString = doc.select(CURRENT_SELECTOR).text();

            return Double.parseDouble(rateToString.replace(",", ""));
        } catch (IOException e) {
            // Jsoup로 crawling을 했을때 반환값이 0일경우 아래와 같은 Error Message가 발생함.
            // 에러 확인 후 코드 추가 -> 아직 해당 에러 재발생하지 않음. 추후에 수정.
            if (e.getMessage().contains("Underlying input stream returned zero bytes")) {
                log.error("Google finance Crawling Error: Underlying input stream returned zero bytes");
            } else {
                log.error("Google finance Crawling Error", e);
            }
            return 0;
        }
    }

    @Scheduled(fixedDelay = 1000 * 10) // 10sec
    private void publishUsdToKrw() {
        double currentRate = getUsdToKrw();

        if (rate == currentRate || currentRate == 0) return;

        log.info("[Update USD to KRW] {} -> {}", rate, currentRate);

        rate = currentRate;

        JsonNode rateTojsonNode = objectMapper.valueToTree(rate);

        emitter.emit("updateUsdToKrw", rateTojsonNode);
    }
}