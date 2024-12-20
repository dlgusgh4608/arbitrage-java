package main.arbitrage.domain.oauthUser.store;

import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;
import main.arbitrage.presentation.dto.view.OAuthSignupView;

@Component
public class OAuthUserStore {
    private final ConcurrentHashMap<String, OAuthSignupView> store = new ConcurrentHashMap<>();

    public void save(String providerId, OAuthSignupView result) {
        store.put(providerId, result);
    }

    public OAuthSignupView getAndRemove(String providerId) {
        return store.remove(providerId);
    }
}
