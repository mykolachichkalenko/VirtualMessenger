package com.example.VirtualMessenger.Controllers;

import com.example.VirtualMessenger.DTOs.LoginRequest;
import com.example.VirtualMessenger.DTOs.PhoneRequest;
import com.example.VirtualMessenger.Services.JWTUtils;
import com.example.VirtualMessenger.Services.OtpService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.r2dbc.core.DatabaseClient;

import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final OtpService otpService;
    private final JWTUtils jwtUtils;
    private final DatabaseClient databaseClient;
//generation code for number
    @PostMapping("/request-otp")
    public Mono<ResponseEntity<String>> requestOtp(@RequestBody PhoneRequest request) {
        return otpService.sendOtp(request.phone())
                .map(result -> ResponseEntity.ok("OTP sent"));
    }
//authentication user
    @PostMapping("/verify-otp")
    public Mono<ResponseEntity<String>> verifyOtp(@RequestBody LoginRequest request) {
        synchronized (request.phone().intern()){
            return otpService.verifyOtp(request.phone(), request.code())
                    .flatMap(valid -> {
                        if (!valid) {
                            // явно Mono<ResponseEntity<String>>
                            return Mono.just(ResponseEntity
                                    .status(HttpStatus.UNAUTHORIZED)
                                    .body("Invalid code"));
                        }

                        String token = jwtUtils.generateToken(request.phone());

                        ResponseCookie cookie = ResponseCookie.from("jwt", token)
                                .httpOnly(true)
                                .secure(false)
                                .path("/")
                                .sameSite("Lax")
                                .maxAge(Duration.ofMinutes(43800))
                                .build();

                        return otpService.createUserIfExists(request.phone())
                                .thenReturn(ResponseEntity.ok()
                                        .header(HttpHeaders.SET_COOKIE, cookie.toString())
                                        .body("SUCCESS"));
                    });
    }}
    //log outs
    @PostMapping("/logout")
    public Mono<Void> logout(ServerHttpResponse response) {
        ResponseCookie deleteCookie = ResponseCookie.from("jwt", "")
                .maxAge(0)
                .path("/")
                .build();

        response.addCookie(deleteCookie);
        return response.setComplete();
    }
    //checking if authenticated
    @GetMapping("/check")
    public boolean checkIfAuthenticated(ServerHttpRequest response){
        HttpCookie cookie = response.getCookies().getFirst("jwt");
        if (cookie == null) return false;
        String token = cookie.getValue();

        return jwtUtils.isValid(token);
    }
}
