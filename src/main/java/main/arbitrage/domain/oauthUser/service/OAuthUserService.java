package main.arbitrage.domain.oauthUser.service;

import lombok.RequiredArgsConstructor;
import main.arbitrage.domain.oauthUser.entity.OAuthUser;
import main.arbitrage.domain.oauthUser.repository.OAuthUserRepository;
import main.arbitrage.domain.user.dto.UserSignupDto;
import main.arbitrage.domain.user.entity.User;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OAuthUserService {
    private final OAuthUserRepository oAuthUserRepository;

    public OAuthUser create(User user, UserSignupDto req) {
        return oAuthUserRepository.save(
                OAuthUser.builder()
                        .providerId(req.getProviderId())
                        .provider(req.getProvider())
                        .user(user)
                        .build()
        );
    }
}
