package main.arbitrage.application.user.service;

import jakarta.transaction.Transactional;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import main.arbitrage.auth.jwt.JwtUtil;
import main.arbitrage.auth.security.SecurityUtil;
import main.arbitrage.domain.exchangeRate.entity.ExchangeRate;
import main.arbitrage.domain.exchangeRate.service.ExchangeRateService;
import main.arbitrage.domain.oauthUser.service.OAuthUserService;
import main.arbitrage.domain.user.entity.User;
import main.arbitrage.domain.user.service.UserService;
import main.arbitrage.domain.userEnv.entity.UserEnv;
import main.arbitrage.domain.userEnv.service.UserEnvService;
import main.arbitrage.global.util.aes.AESCrypto;
import main.arbitrage.infrastructure.email.dto.EmailMessageDTO;
import main.arbitrage.infrastructure.email.service.EmailMessageService;
import main.arbitrage.infrastructure.exchange.binance.dto.response.BinanceGetAccountResponse;
import main.arbitrage.infrastructure.exchange.dto.ExchangePrivateRestPair;
import main.arbitrage.infrastructure.exchange.factory.ExchangePrivateRestFactory;
import main.arbitrage.infrastructure.exchange.upbit.dto.response.UpbitAccountResponse;
import main.arbitrage.infrastructure.oauthValidator.service.OauthValidatorService;
import main.arbitrage.infrastructure.redis.service.RefreshTokenService;
import main.arbitrage.presentation.dto.form.UserEnvForm;
import main.arbitrage.presentation.dto.form.UserLoginForm;
import main.arbitrage.presentation.dto.form.UserSignupForm;
import main.arbitrage.presentation.dto.request.EditUserNicknameRequest;
import main.arbitrage.presentation.dto.response.UserTokenResponseCookie;
import main.arbitrage.presentation.dto.view.UserProfileView;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserApplicationService {
  private final EmailMessageService emailMessageService;
  private final UserService userService;
  private final OAuthUserService oAuthUserService;
  private final ExchangeRateService exchangeRateService;
  private final RefreshTokenService refreshTokenService;
  private final UserEnvService userEnvService;
  private final AESCrypto aesCrypto;

  private final JwtUtil jwtUtil;

  private final OauthValidatorService oauthValidatorService;

  private final ExchangePrivateRestFactory exchangePrivateRestFactory;

  @Transactional
  public String sendEmail(String email) {
    userService.existsByEmail(email);

    String code =
        emailMessageService.sendMail(
            EmailMessageDTO.builder().to(email).subject("[Arbitrage] 이메일 인증을 위한 인증 코드 발송").build(),
            "email");

    return aesCrypto.encrypt(code.getBytes());
  }

  @Transactional
  public UserTokenResponseCookie signup(UserSignupForm req) {
    String email = req.getEmail();

    userService.existsByEmail(email);

    String code = req.getCode();
    String encryptedCode = req.getEncryptedCode();
    String accessToken = req.getAccessToken();
    String provider = req.getProvider();
    String providerId = req.getProviderId();

    // 일반 회원가입
    if (!code.isEmpty() && !encryptedCode.isEmpty()) {

      aesCrypto.check(encryptedCode, code);
      return userTokenResponseBuilder(userService.create(req.getEmail(), req.getPassword()));
    }

    // OAuth 회원가입.
    oauthValidatorService.validate(provider, accessToken, providerId, email);

    User user = userService.create(req.getEmail(), req.getPassword());
    oAuthUserService.create(user, req);

    return userTokenResponseBuilder(user);
  }

  @Transactional
  public UserTokenResponseCookie login(UserLoginForm req) {
    User user = userService.findAndExistByEmail(req.getEmail());

    userService.matchPassword(req.getPassword(), user.getPassword());

    return userTokenResponseBuilder(user);
  }

  @Transactional
  public void registerUserEnv(UserEnvForm req) {
    Long userId = SecurityUtil.getUserId();

    User user = userService.findAndExistByEmail(SecurityUtil.getEmail());

    // upbit accessKey와 secretKey를 지갑 잔액을 조회함으로써 올바른 키가 맞는지 증명
    ExchangePrivateRestPair upbitExchangePrivateRestPair =
        exchangePrivateRestFactory.create(
            req.getUpbitAccessKey(),
            req.getUpbitSecretKey(),
            req.getBinanceAccessKey(),
            req.getBinanceSecretKey());

    upbitExchangePrivateRestPair.getUpbit().getAccount();
    upbitExchangePrivateRestPair.getBinance().getAccount();

    Optional<UserEnv> optionalUserEnv = userEnvService.findByUserId(userId);

    // 새로 등록
    if (optionalUserEnv.isEmpty()) {
      userEnvService.create(UserEnvForm.toEntity(req, user, aesCrypto));
      return;
    }

    // 업데이트
    UserEnv userEnv = optionalUserEnv.get();
    userEnv.updateEnv(req, aesCrypto);
  }

  @Transactional
  public UserProfileView getUserProfile() {
    Long userId = SecurityUtil.getUserId();

    // userId에 해당하는 env가 있는지 확인
    Optional<UserEnv> userEnvOptional = userEnvService.findByUserId(userId);

    // builder 변수 할당
    UserProfileView.UserProfileViewBuilder userProfileDtoBuilder = UserProfileView.builder();

    if (userEnvOptional.isEmpty()) {
      // env가 없으면 upbit와 binance의 wallet 정보를 받아올 수 없음.
      userProfileDtoBuilder.binanceBalance(null).upbitBalance(null);
    } else {
      // env를 통하여 거래소의 service를 생성
      UserEnv userEnv = userEnvOptional.get();
      ExchangePrivateRestPair upbitExchangePrivateRestPair =
          exchangePrivateRestFactory.create(userEnv);

      // 각각 거래소의 지갑정보를 받아옴
      Optional<UpbitAccountResponse> upbitKRW = upbitExchangePrivateRestPair.getUpbit().getKRW();
      Optional<BinanceGetAccountResponse> binanceUSDT =
          upbitExchangePrivateRestPair.getBinance().getUSDT();

      // builder에 build하기
      if (upbitKRW.isEmpty()) {
        userProfileDtoBuilder.upbitBalance(null);
      } else {
        userProfileDtoBuilder.upbitBalance(Double.parseDouble(upbitKRW.get().balance()));
      }

      if (binanceUSDT.isEmpty()) {
        userProfileDtoBuilder.binanceBalance(null);
      } else {
        userProfileDtoBuilder.binanceBalance(Double.parseDouble(binanceUSDT.get().balance()));
      }
    }

    ExchangeRate exchangeRate = exchangeRateService.getNonNullUsdToKrw();

    userProfileDtoBuilder.nickname(SecurityUtil.getNickname());
    userProfileDtoBuilder.exchangeRate(exchangeRate.getRate());

    return userProfileDtoBuilder.build();
  }

  @Transactional
  public String updateNickname(EditUserNicknameRequest req) {
    Long userId = SecurityUtil.getUserId();
    String nickname = req.nickname();

    User user = userService.findAndExistByUserId(userId);

    userService.updateNickname(user, nickname);

    return jwtUtil.createToken(
        user.getId(), user.getEmail(), user.getNickname(), SecurityUtil.getExpiredAt());
  }

  private UserTokenResponseCookie userTokenResponseBuilder(User user) {
    String accessToken = jwtUtil.createToken(user.getId(), user.getEmail(), user.getNickname());
    String refreshToken =
        refreshTokenService.createRefreshToken(user.getEmail(), UUID.randomUUID().toString());
    Long refreshTokenTTL = refreshTokenService.getRefreshTokenTTL(user.getEmail());

    return UserTokenResponseCookie.builder()
        .accessToken(accessToken)
        .refreshToken(refreshToken)
        .refreshTokenTTL(refreshTokenTTL)
        .build();
  }
}
