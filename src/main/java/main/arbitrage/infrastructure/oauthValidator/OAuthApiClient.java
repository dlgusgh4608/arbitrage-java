package main.arbitrage.infrastructure.oauthValidator;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

@HttpExchange(contentType = "application/json", accept = "application/json")
public interface OAuthApiClient {

  // google
  @GetExchange("https://www.googleapis.com/oauth2/v3/userinfo")
  public JsonNode getGoogleInfo(@RequestHeader("Authorization") String accessKey);

  // kakao
  @GetExchange("https://kapi.kakao.com/v2/user/me")
  public JsonNode getKakaoInfo(@RequestHeader("Authorization") String accessKey);
}
