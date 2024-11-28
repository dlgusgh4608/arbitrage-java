package main.arbitrage.domain.oauthUser.service;

import lombok.RequiredArgsConstructor;
import main.arbitrage.domain.oauthUser.entity.OAuthUser;
import main.arbitrage.domain.oauthUser.repository.OAuthUserRepository;
import main.arbitrage.domain.user.entity.User;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class OAuthUserService {
    private final OAuthUserRepository oAuthUserRepository;

    public OAuthUser create(OAuthUser oAuthUser) {
        return oAuthUserRepository.save(oAuthUser);
    }
}
