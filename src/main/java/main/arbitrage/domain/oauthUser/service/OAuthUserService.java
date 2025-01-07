package main.arbitrage.domain.oauthUser.service;

import lombok.RequiredArgsConstructor;
import main.arbitrage.domain.oauthUser.entity.OAuthUser;
import main.arbitrage.domain.oauthUser.exception.OAuthUserErrorCode;
import main.arbitrage.domain.oauthUser.exception.OAuthUserException;
import main.arbitrage.domain.oauthUser.repository.OAuthUserRepository;
import main.arbitrage.domain.user.entity.User;
import main.arbitrage.presentation.dto.form.UserSignupForm;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OAuthUserService {
  private final OAuthUserRepository oAuthUserRepository;

  public OAuthUser create(User user, UserSignupForm req) {
    try {
      return oAuthUserRepository.save(
          OAuthUser.builder()
              .providerId(req.getProviderId())
              .provider(req.getProvider())
              .user(user)
              .build());
    } catch (Exception e) {
      throw new OAuthUserException(OAuthUserErrorCode.UNKNOWN, "OAuthUser Create Error", e);
    }
  }
}
