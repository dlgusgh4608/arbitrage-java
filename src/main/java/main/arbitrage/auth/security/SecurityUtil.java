package main.arbitrage.auth.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class SecurityUtil {

    public static Long getUserId() {
        CustomUserDetails userDetails = isAuthorized();
        return userDetails.getUserId();
    }

    public static String getEmail() {
        CustomUserDetails userDetails = isAuthorized();
        return userDetails.getEmail();
    }

    private static CustomUserDetails isAuthorized() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        if (!authentication.isAuthenticated()) {
            throw new RuntimeException("Unauthorized");
        }

        return userDetails;
    }
}