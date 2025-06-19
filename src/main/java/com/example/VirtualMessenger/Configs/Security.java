package com.example.VirtualMessenger.Configs;

import com.example.VirtualMessenger.Services.JWTUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationFilter;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.AuthenticationWebFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;
import reactor.core.publisher.Mono;

import java.util.List;

@Configuration
@EnableWebFluxSecurity
public class Security {
private final JWTUtils jwtUtils;

    public Security(JWTUtils jwtUtils) {
        this.jwtUtils = jwtUtils;
    }

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http){
    return http
            .csrf(ServerHttpSecurity.CsrfSpec::disable)
            .formLogin(ServerHttpSecurity.FormLoginSpec::disable)
            .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .authorizeExchange(exchange -> exchange
                    .pathMatchers("/auth/**").permitAll()
                    .anyExchange().authenticated())
            .addFilterAt(authenticationWebFilter(),SecurityWebFiltersOrder.AUTHENTICATION)
            .build();
}

    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        config.addAllowedOrigin("http://localhost:5173,http://localhost:5174");
        config.addAllowedHeader("*");
        config.addAllowedMethod("*");
        config.addExposedHeader(HttpHeaders.AUTHORIZATION);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    public AuthenticationWebFilter authenticationWebFilter(){
        ReactiveAuthenticationManager authenticationManager = Mono::just;
        AuthenticationWebFilter filter = new AuthenticationWebFilter(authenticationManager);

        filter.setServerAuthenticationConverter(exchange -> {
            HttpCookie httpCookie = exchange.getRequest().getCookies().getFirst("jwt");
            if (httpCookie != null){
                String token = httpCookie.getValue();
                try {
                    String phone = jwtUtils.extractPhone(token);
                    Authentication authentication = new UsernamePasswordAuthenticationToken(phone,null, List.of());
                    return Mono.just(authentication);
                } catch (Exception e){
                    return Mono.empty();
                }
            }
            return Mono.empty();
        });
            return filter;
    }
}
