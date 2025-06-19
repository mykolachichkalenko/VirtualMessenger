package com.example.VirtualMessenger.Services;

import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class OtpService {
    private final Map<String, String> otpStore = new ConcurrentHashMap<>();
    private final DatabaseClient databaseClient;

    public OtpService(DatabaseClient databaseClient) {
        this.databaseClient = databaseClient;
    }

    public Mono<Boolean> sendOtp(String phone) {
        String code = generateOtp();
        otpStore.put(phone, code);
        System.out.println(code + " is a code for " + phone);
        return Mono.just(true);
    }

    public Mono<Boolean> verifyOtp(String phone, String code) {
        return Mono.just(code.equals(otpStore.get(phone)));
    }

    private String generateOtp() {
        return String.valueOf((int) (Math.random() * 900000) + 100000);
    }

    public Mono<Void> createUserIfExists(String phone){

        String findUser = "SELECT EXISTS (SELECT 1 FROM users WHERE phone_number = :phone)";
        String createUser = "INSERT INTO users(phone_number) VALUES(:phone)";

        return databaseClient.sql(findUser)
                .bind("phone",phone)
                .map((row, rowMetadata) -> {
                    Long value = row.get(0, Long.class); // <--- замість Boolean.class
                    return value != null && value == 1L;  // <--- перетворення вручну
                })                .one()
                .flatMap(exists -> {
                    if (Boolean.TRUE.equals(exists)){
                        return Mono.empty();
                    }else{
                        return databaseClient.sql(createUser)
                                 .bind("phone",phone)
                                .then();
                    }
                });
    }
}