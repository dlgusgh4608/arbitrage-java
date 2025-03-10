package main.arbitrage.domain.price.buffer;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import lombok.extern.slf4j.Slf4j;
import main.arbitrage.domain.price.entity.Price;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class PriceBuffer {
  private final List<Price> buffer = new CopyOnWriteArrayList<>();

  public void add(Price price) {
    buffer.add(price);
  }

  public List<Price> getBufferedData() {
    return new ArrayList<>(buffer);
  }

  public void clear() {
    buffer.clear();
  }

  public boolean isEmpty() {
    return buffer.isEmpty();
  }

  public int size() {
    return buffer.size();
  }

  public List<Price> getBufferedDataOfSymbol(String symbol) {
    return new ArrayList<>(buffer)
        .stream().filter(price -> price.getSymbol().equals(symbol)).toList();
  }
}
