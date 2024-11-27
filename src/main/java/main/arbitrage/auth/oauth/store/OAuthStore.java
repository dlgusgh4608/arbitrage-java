package main.arbitrage.auth.oauth.store;

import main.arbitrage.auth.oauth.dto.OAuthDto;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;

@Component
public class OAuthStore {
    private final ConcurrentHashMap<String, OAuthDto> store = new ConcurrentHashMap<>();

    public void save(String providerId, OAuthDto result) {
        store.put(providerId, result);
    }

    public OAuthDto getAndRemove(String providerId) {
        return store.remove(providerId);
    }
}