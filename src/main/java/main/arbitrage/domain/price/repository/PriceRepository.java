package main.arbitrage.domain.price.repository;

import java.util.List;
import main.arbitrage.domain.price.entity.Price;
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
}
