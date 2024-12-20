package main.arbitrage.global.util.json;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TypedJsonNode<T> {
    private final JsonNode jsonNode;
    private final Class<T> type;

    public T convertToType(ObjectMapper objectMapper) {
        return objectMapper.convertValue(jsonNode, type);
    }

    public static <T> TypedJsonNode<T> of(JsonNode jsonNode, Class<T> type) {
        return new TypedJsonNode<>(jsonNode, type);
    }
}
