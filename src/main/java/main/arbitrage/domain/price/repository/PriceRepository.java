package main.arbitrage.domain.price.repository;

import org.springframework.data.repository.query.Param;
import main.arbitrage.domain.price.entity.Price;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PriceRepository extends JpaRepository<Price, Long> {
    @Query("""
            SELECT p
            FROM Price p
            JOIN p.symbol s
            WHERE s.name = :symbolName
            ORDER BY p.id DESC
            """)
    List<Price> findBySymbolOfPageable(
            @Param("symbolName") String symbolName,
            Pageable pageable
    );
}