package main.arbitrage.domain.user.controller;

import lombok.RequiredArgsConstructor;
import main.arbitrage.domain.user.dto.request.UserLoginRequest;
import main.arbitrage.domain.user.dto.request.UserRegisterRequest;
import main.arbitrage.domain.user.dto.response.UserLoginResponse;
import main.arbitrage.domain.user.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import main.arbitrage.auth.security.SecurityUtil;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody UserRegisterRequest request) {
        userService.register(request);

        return ResponseEntity.ok().build();
    }

    @PostMapping("/login")
    public ResponseEntity<UserLoginResponse> login(@RequestBody UserLoginRequest request) {
        UserLoginResponse userLoginResponse = userService.login(request);

        return ResponseEntity.ok(userLoginResponse);
    }

    @GetMapping("/test")
    public ResponseEntity<UserLoginResponse> refresh() {
        Long userId = SecurityUtil.getUserId();
        String email = SecurityUtil.getEmail();
        String nickname = SecurityUtil.getNickname();
        System.out.println(userId);
        System.out.println(email);
        System.out.println(nickname);

        return null;
    }
}