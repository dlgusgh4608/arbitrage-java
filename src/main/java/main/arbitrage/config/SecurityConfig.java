package main.arbitrage.config;

import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import lombok.RequiredArgsConstructor;
import main.arbitrage.auth.jwt.JwtFilter;
import main.arbitrage.auth.oauth.handler.OAuthSuccessHandler;
import main.arbitrage.auth.oauth.repository.OAuthUserRequestRepository;
import main.arbitrage.auth.oauth.service.OAuthUserRequestService;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private final JwtFilter jwtFilter;
    private final OAuthUserRequestRepository oAuthUserRequestRepository;
    private final OAuthUserRequestService oAuthUserRequestService;
    private final OAuthSuccessHandler oAuthSuccessHandler;

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return (web) ->
                web.ignoring().requestMatchers(PathRequest.toStaticResources().atCommonLocations());
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
                .csrf((csrf) ->
                        csrf.disable()
                )
                .sessionManagement((session) ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authorizeHttpRequests((authorizeRequests) ->
                        authorizeRequests
                                .requestMatchers("/").permitAll() // main page
                                .requestMatchers("/chart/**").permitAll() // chart
                                .requestMatchers("/ws/**").permitAll()
                                .requestMatchers("/login/**", "/api/users/login/**").permitAll() // login
                                .requestMatchers("/signup/**", "/api/users/signup/**").permitAll() // signup
                                .requestMatchers("/logout/**", "/api/users/logout/**").permitAll() // logout
                                .anyRequest().authenticated()
                )
                .oauth2Login(oauth2 ->
                        oauth2
                                .loginPage("/login")
                                .loginPage("/signup")
                                .authorizationEndpoint(endpoint ->
                                        endpoint.authorizationRequestRepository(oAuthUserRequestRepository)
                                )
                                .userInfoEndpoint(userInfo ->
                                        userInfo.userService(oAuthUserRequestService)
                                )
                                .successHandler(oAuthSuccessHandler)
                )
                .logout(logout ->
                        logout
                                .logoutUrl("/api/users/logout")
                                .clearAuthentication(true)
                                .deleteCookies("accessToken", "refreshToken")
                                .logoutSuccessUrl("/")
                )
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}