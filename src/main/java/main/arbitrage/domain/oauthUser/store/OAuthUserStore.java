package main.arbitrage.domain.oauthUser.store;

import main.arbitrage.domain.oauthUser.dto.OAuthUserDto;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;

@Component
public class OAuthUserStore {
    private final ConcurrentHashMap<String, OAuthUserDto> store = new ConcurrentHashMap<>();

    public void save(String providerId, OAuthUserDto result) {
        store.put(providerId, result);
    }

    public OAuthUserDto getAndRemove(String providerId) {
        return store.remove(providerId);
    }
}