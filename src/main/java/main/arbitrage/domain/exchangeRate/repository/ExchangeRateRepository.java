package main.arbitrage.domain.exchangeRate.repository;

import main.arbitrage.domain.exchangeRate.entity.ExchangeRate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ExchangeRateRepository extends JpaRepository<ExchangeRate, Long> {

//    Optional<ExchangeRate> findByCurrency(String fromCurrency, String toCurrency);

//    ExchangeRate findLastByCurrency(String fromCurrency, String toCurrency);
//
//    ExchangeRate insertByCurrency(String fromCurrency, String toCurrency);

//    boolean existsByCurrency(String fromCurrency, String toCurrency);
}