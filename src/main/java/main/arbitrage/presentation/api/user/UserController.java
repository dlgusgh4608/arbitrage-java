package main.arbitrage.presentation.api.user;

import lombok.RequiredArgsConstructor;
import main.arbitrage.domain.user.dto.request.UserLoginRequest;
import main.arbitrage.domain.user.dto.request.UserRegisterRequest;
import main.arbitrage.domain.user.dto.response.UserLoginResponse;
import main.arbitrage.domain.user.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
}