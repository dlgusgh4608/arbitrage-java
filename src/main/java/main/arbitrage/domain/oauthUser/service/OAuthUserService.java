package main.arbitrage.domain.oauthUser.service;

import lombok.RequiredArgsConstructor;
import main.arbitrage.domain.oauthUser.entity.OAuthUser;
import main.arbitrage.domain.oauthUser.repository.OAuthUserRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class OAuthUserService {
    private final OAuthUserRepository oAuthUserRepository;
}
