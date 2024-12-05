
package main.arbitrage.domain.symbol.service;

import lombok.RequiredArgsConstructor;
import main.arbitrage.domain.symbol.entity.Symbol;
import main.arbitrage.domain.symbol.respository.SymbolRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class SymbolVariableService implements CommandLineRunner {
    private final SymbolRepository symbolRepository;
    private static final List<Symbol> symbols = new ArrayList<>();
    private static volatile boolean initialized = false;
    private static final Object lock = new Object();

    @Override
    public void run(String... args) {
        initializeIfNeeded();
    }

    private void initializeIfNeeded() {
        if (!initialized) {
            synchronized (lock) {
                if (!initialized) {
                    initializedSymbol("btc");
                    initializedSymbol("eth");
                    setSymbols();
                    initialized = true;
                }
            }
        }
    }

    private void initializedSymbol(String name) {
        if (!symbolRepository.existsByName(name)) {
            Symbol symbol = Symbol.builder()
                    .name(name)
                    .use(true)
                    .build();

            symbolRepository.save(symbol);
        }
    }

    private void setSymbols() {
        symbols.clear();
        symbols.addAll(symbolRepository.findAll());
    }

    public List<Symbol> getAllSymbol() {
        initializeIfNeeded();
        return new ArrayList<>(symbols);
    }

    public List<Symbol> getSupportedSymbols() {
        initializeIfNeeded();
        return symbols.stream()
                .filter(Symbol::isUse)
                .toList();
    }
}