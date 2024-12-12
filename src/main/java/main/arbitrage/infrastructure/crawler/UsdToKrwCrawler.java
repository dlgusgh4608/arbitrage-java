package main.arbitrage.infrastructure.crawler;

import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@Slf4j
public class UsdToKrw {
    private static final String GOOGLE_FINANCE_USD_TO_KRW_URL = "https://www.google.com/finance/quote/USD-KRW";
    private static final String CURRENT_SELECTOR = "div.YMlKec.fxKbKc"; // 환율 셀렉터 -> 나중에 구글이 수정하면 바꿔야함.

    public double craw() {
        try {
            Document doc = Jsoup.connect(GOOGLE_FINANCE_USD_TO_KRW_URL).get();

            String rateToString = doc.select(CURRENT_SELECTOR).text();

            if (rateToString.isEmpty()) return 0;

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
}