package main.arbitrage.domain.price.buffer;

import lombok.extern.slf4j.Slf4j;
import main.arbitrage.domain.price.entity.Price;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Slf4j
@Component
public class PriceBuffer {
    private final List<Price> buffer = new CopyOnWriteArrayList<>();
    private static final int BUFFER_SIZE = 60;

    public void add(Price price) {
        buffer.add(price);
    }

    public List<Price> getBufferedData() {
        return new ArrayList<>(buffer);
    }

    public void clear() {
        buffer.clear();
    }

    public boolean isReadyToSave() {
        return buffer.size() >= BUFFER_SIZE;
    }

    public boolean isEmpty() {
        return buffer.isEmpty();
    }

    public int size() {
        return buffer.size();
    }
}