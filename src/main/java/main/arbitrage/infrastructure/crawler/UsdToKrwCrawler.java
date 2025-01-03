package main.arbitrage.infrastructure.crawler;

import java.io.IOException;
import main.arbitrage.infrastructure.crawler.exception.CrawlerErrorCode;
import main.arbitrage.infrastructure.crawler.exception.CrawlerException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Component;

@Component
public class UsdToKrwCrawler {
  private static final String GOOGLE_FINANCE_USD_TO_KRW_URL =
      "https://www.google.com/finance/quote/USD-KRW";
  private static final String CURRENT_SELECTOR = "div.YMlKec.fxKbKc"; // 환율 셀렉터

  public double craw() {
    try {
      Document doc = Jsoup.connect(GOOGLE_FINANCE_USD_TO_KRW_URL).get();

      String rateToString = doc.select(CURRENT_SELECTOR).text();

      if (rateToString.isEmpty()) return 0;

      return Double.parseDouble(rateToString.replace(",", ""));
    } catch (IOException e) {
      throw new CrawlerException(CrawlerErrorCode.ZERO_BYTE);
    }
  }
}
