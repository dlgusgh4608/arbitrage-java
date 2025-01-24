package main.arbitrage.domain.symbol.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import main.arbitrage.domain.symbol.entity.Symbol;
import main.arbitrage.domain.symbol.exception.SymbolErrorCode;
import main.arbitrage.domain.symbol.exception.SymbolException;
import main.arbitrage.domain.symbol.repository.SymbolRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SymbolVariableService implements CommandLineRunner {
  private final List<String> DEFAULT_SYMBOLS = List.of("BTC", "ETH");

  private final SymbolRepository symbolRepository;
  private Map<String, Symbol> supportedSymbolMap = new ConcurrentHashMap<>();
  private static volatile boolean initialized = false;

  @Override
  public void run(String... args) {
    try {
      initializeIfNeeded();
    } catch (Exception e) {
      throw new SymbolException(SymbolErrorCode.INITIALIZED_FAILED, "애플리케이션 시작시 심볼 초기화 실패", e);
    }
  }

  private void initializeIfNeeded() {
    if (!initialized) {
      synchronized (SymbolVariableService.class) {
        if (!initialized) {
          try {
            initializeSymbols();
            initialized = true;
          } catch (Exception e) {
            throw new SymbolException(SymbolErrorCode.INITIALIZED_FAILED, "심볼 초기화 도중 오류", e);
          }
        }
      }
    }
  }

  private void initializeSymbols() {
    try {
      DEFAULT_SYMBOLS.forEach(this::initializeSymbol);
      supportedSymbolMap =
          symbolRepository.findByUseTrue().stream()
              .collect(Collectors.toConcurrentMap(Symbol::getName, symbol -> symbol));
    } catch (Exception e) {
      throw new SymbolException(SymbolErrorCode.INITIALIZED_FAILED, "심볼 로딩 오류", e);
    }
  }

  private void initializeSymbol(String name) {
    try {
      if (!symbolRepository.existsByName(name)) {
        symbolRepository.save(Symbol.builder().name(name).use(true).build());
      }
    } catch (Exception e) {
      throw new SymbolException(
          SymbolErrorCode.INITIALIZED_FAILED, String.format("심볼 '%s' 초기화 중 오류 발생", name), e);
    }
  }

  public List<Symbol> getAllSymbol() {
    try {
      return symbolRepository.findAll();
    } catch (SymbolException e) {
      throw e;
    } catch (Exception e) {
      throw new SymbolException(SymbolErrorCode.INITIALIZED_FAILED, "전체 심볼 조회 중 오류 발생", e);
    }
  }

  public List<Symbol> getSupportedSymbols() {
    try {
      initializeIfNeeded();
      return new ArrayList<>(supportedSymbolMap.values());
    } catch (SymbolException e) {
      throw e;
    } catch (Exception e) {
      throw new SymbolException(SymbolErrorCode.INITIALIZED_FAILED, "지원 심볼 조회 중 오류 발생", e);
    }
  }

  public List<String> getSupportedSymbolNames() {
    try {
      initializeIfNeeded();
      return new ArrayList<>(supportedSymbolMap.keySet());
    } catch (SymbolException e) {
      throw e;
    } catch (Exception e) {
      throw new SymbolException(SymbolErrorCode.INITIALIZED_FAILED, "지원 심볼 이름 조회 중 오류 발생", e);
    }
  }

  public Symbol findSymbolByName(String name) {
    try {
      if (name == null || name.trim().isEmpty()) {
        throw new SymbolException(SymbolErrorCode.EMPTY_SYMBOL, "심볼 이름이 비어있습니다");
      }

      initializeIfNeeded();

      return supportedSymbolMap.get(name);
    } catch (Exception e) {
      throw new SymbolException(
          SymbolErrorCode.INITIALIZED_FAILED, String.format("심볼 '%s' 검색 중 오류 발생", name), e);
    }
  }

  public Symbol findAndExistSymbolByName(String name) {
    try {
      Symbol symbol = findSymbolByName(name);
      if (symbol == null)
        throw new SymbolException(
            SymbolErrorCode.NOT_FOUND_SYMBOL, String.format("심볼 '%s'을 찾을 수 없습니다", name));

      return symbol;
    } catch (SymbolException e) {
      throw e;
    } catch (Exception e) {
      throw new SymbolException(
          SymbolErrorCode.INITIALIZED_FAILED, String.format("심볼 '%s' 검색 중 오류 발생", name), e);
    }
  }

  public boolean isSupportedSymbol(String name) {
    try {
      return findSymbolByName(name) == null ? false : true;
    } catch (SymbolException e) {
      throw e;
    } catch (Exception e) {
      throw new SymbolException(SymbolErrorCode.INITIALIZED_FAILED, "심볼 지원 여부 확인중 오류", e);
    }
  }
}
