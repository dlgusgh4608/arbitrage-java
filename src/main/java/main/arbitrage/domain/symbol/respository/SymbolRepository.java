package main.arbitrage.domain.symbol.respository;

import main.arbitrage.domain.symbol.entity.Symbol;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SymbolRepository extends JpaRepository<Symbol, Long> {
  boolean existsByName(String name);
}
