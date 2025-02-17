package main.arbitrage.infrastructure.oauthValidator;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import lombok.RequiredArgsConstructor;
import main.arbitrage.domain.oauthUser.exception.OAuthUserErrorCode;
import main.arbitrage.domain.oauthUser.exception.OAuthUserException;
import main.arbitrage.global.exception.common.BaseHttpErrorHandler;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class OAuthApiErrorHandler extends BaseHttpErrorHandler {
  private final ObjectMapper objectMapper;

  @Override
  public boolean hasError(ClientHttpResponse response) {
    try {
      return response.getStatusCode().isError();
    } catch (Exception e) {
      throw new OAuthUserException(OAuthUserErrorCode.UNKNOWN, e);
    }
  }

  @Override
  public void handleError(ClientHttpResponse response) {
    URI uri = requestContext.get().request().getURI();
    String body = requestContext.get().body();

    String errorMsg = String.format("\nurl: %s\nbody: %s", uri.toString(), body);
    try {
      throw new OAuthUserException(OAuthUserErrorCode.UNKNOWN, errorMsg);
    } catch (OAuthUserException e) {
      throw e;
    } finally {
      requestContext.remove();
    }
  }
}
