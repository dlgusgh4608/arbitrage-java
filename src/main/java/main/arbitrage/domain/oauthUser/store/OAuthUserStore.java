package main.arbitrage.domain.oauthUser.store;

import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;
import main.arbitrage.domain.oauthUser.dto.OAuthUserDto;

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
