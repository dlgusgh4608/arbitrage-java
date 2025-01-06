package main.arbitrage.domain.symbol.service;

import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import main.arbitrage.domain.symbol.entity.Symbol;
import main.arbitrage.domain.symbol.exception.SymbolErrorCode;
import main.arbitrage.domain.symbol.exception.SymbolException;
import main.arbitrage.domain.symbol.respository.SymbolRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SymbolVariableService implements CommandLineRunner {
  private final List<String> DEFAULT_SYMBOLS = List.of("BTC", "ETH");

  private final SymbolRepository symbolRepository;
  private static final List<Symbol> symbols = new ArrayList<>();
  private static volatile boolean initialized = false;

  @Override
  public void run(String... args) {
    initializeIfNeeded();
  }

  private void initializeIfNeeded() {
    if (!initialized) {
      synchronized (SymbolVariableService.class) {
        if (!initialized) {
          initializeSymbols();
          initialized = true;
        }
      }
    }
  }

  private void initializeSymbols() {
    DEFAULT_SYMBOLS.forEach(this::initializeSymbol);
    symbols.clear();
    symbols.addAll(symbolRepository.findAll());
  }

  private void initializeSymbol(String name) {
    if (!symbolRepository.existsByName(name)) {
      symbolRepository.save(Symbol.builder().name(name).use(true).build());
    }
  }

  public List<Symbol> getAllSymbol() {
    initializeIfNeeded();
    return new ArrayList<>(symbols);
  }

  public List<Symbol> getSupportedSymbols() {
    initializeIfNeeded();
    return symbols.stream().filter(Symbol::isUse).toList();
  }

  public List<String> getSupportedSymbolNames() {
    return getSupportedSymbols().stream().map(symbol -> symbol.getName().toUpperCase()).toList();
  }

  public Symbol findSymbolByName(String name) {
    initializeIfNeeded();
    return symbols.stream()
        .filter(symbol -> symbol.getName().toUpperCase().equalsIgnoreCase(name.toUpperCase()))
        .findFirst()
        .orElseThrow(() -> new SymbolException(SymbolErrorCode.NOT_FOUND_SYMBOL));
  }
}
