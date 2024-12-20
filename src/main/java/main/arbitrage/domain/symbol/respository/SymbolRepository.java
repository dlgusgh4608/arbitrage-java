package main.arbitrage.domain.symbol.respository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import main.arbitrage.domain.symbol.entity.Symbol;

@Repository
public interface SymbolRepository extends JpaRepository<Symbol, Long> {
    boolean existsByName(String name);
}
