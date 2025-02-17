package main.arbitrage.global.exception.common;

import java.nio.charset.StandardCharsets;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.ResponseErrorHandler;

public abstract class BaseHttpErrorHandler implements ResponseErrorHandler {
  protected final ThreadLocal<RequestContext> requestContext = new ThreadLocal<>();

  protected record RequestContext(HttpRequest request, String body) {}

  public void setRequest(HttpRequest request, byte[] body) {
    requestContext.set(new RequestContext(request, new String(body, StandardCharsets.UTF_8)));
  }

  @Override
  public abstract boolean hasError(ClientHttpResponse response);

  @Override
  public abstract void handleError(ClientHttpResponse response);
}
