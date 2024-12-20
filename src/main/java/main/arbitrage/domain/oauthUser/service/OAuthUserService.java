package main.arbitrage.domain.oauthUser.service;

import java.util.List;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import main.arbitrage.domain.oauthUser.entity.OAuthUser;
import main.arbitrage.domain.oauthUser.repository.OAuthUserRepository;
import main.arbitrage.domain.user.dto.UserSignupDto;
import main.arbitrage.domain.user.entity.User;

@Service
@RequiredArgsConstructor
public class OAuthUserService {
    private final OAuthUserRepository oAuthUserRepository;

    public OAuthUser create(User user, UserSignupDto req) {
        return oAuthUserRepository.save(OAuthUser.builder().providerId(req.getProviderId())
                .provider(req.getProvider()).user(user).build());
    }

    public List<OAuthUser> getOAuthUserListByUserId(Long userId) {
        return oAuthUserRepository.findByUserId(userId);
    }
}
