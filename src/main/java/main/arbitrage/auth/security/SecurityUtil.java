package main.arbitrage.auth.security;

import main.arbitrage.auth.dto.AuthContext;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class SecurityUtil {

  public static Long getUserId() {
    AuthContext authContext = isAuthorized();
    return authContext.getUserId();
  }

  public static String getEmail() {
    AuthContext authContext = isAuthorized();
    return authContext.getEmail();
  }

  public static String getNickname() {
    AuthContext authContext = isAuthorized();
    return authContext.getNickname();
  }

  public static Long getExpiredAt() {
    AuthContext authContext = isAuthorized();
    return authContext.getExpiredAt();
  }

  public static AuthContext getContext() {
    return isAuthorized();
  }

  private static AuthContext isAuthorized() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

    AuthContext authContext = (AuthContext) authentication.getPrincipal();

    if (!authentication.isAuthenticated()) {
      throw new BadCredentialsException("Unauthorized");
    }

    return authContext;
  }
}
