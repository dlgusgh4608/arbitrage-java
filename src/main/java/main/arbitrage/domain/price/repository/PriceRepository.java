package main.arbitrage.domain.price.repository;

import java.util.List;
import main.arbitrage.application.auto.dto.AutoTradingStandardValueDTO;
import main.arbitrage.domain.price.entity.Price;
import main.arbitrage.domain.symbol.entity.Symbol;
import main.arbitrage.presentation.dto.response.ChartDataResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PriceRepository
    extends JpaRepository<Price, Long>, PriceQueryRepository, PriceRawQueryRepository {

  @Override
  public void bulkInsert(List<Price> prices);

  @Override
  public List<Price> findBySymbolName(String symbolName, Pageable pageable);

  @Override
  public AutoTradingStandardValueDTO getAutoTradingStandardValue(Symbol symbol, int minutes);

  @Override
  public List<ChartDataResponse> getPremiumOHLC(Symbol symbol, int unit, long lastTime);
}
