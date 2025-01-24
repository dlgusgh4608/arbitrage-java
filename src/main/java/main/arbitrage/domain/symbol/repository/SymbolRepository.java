package main.arbitrage.domain.symbol.repository;

import java.util.List;
import main.arbitrage.domain.symbol.entity.Symbol;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SymbolRepository extends JpaRepository<Symbol, Long> {
  boolean existsByName(String name);

  List<Symbol> findByUseTrue();
}
