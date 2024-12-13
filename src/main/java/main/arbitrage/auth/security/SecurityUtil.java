package main.arbitrage.auth.security;

import main.arbitrage.domain.user.entity.User;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class SecurityUtil {

    public static Long getUserId() {
        User user = isAuthorized();
        return user.getUserId();
    }

    public static String getEmail() {
        User user = isAuthorized();
        return user.getEmail();
    }

    public static String getNickname() {
        User user = isAuthorized();
        return user.getNickname();
    }

    public static User getUser() {
        return isAuthorized();
    }


    private static User isAuthorized() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        User user = (User) authentication.getPrincipal();

        if (!authentication.isAuthenticated()) {
            throw new BadCredentialsException("Unauthorized");
        }

        return user;
    }
}