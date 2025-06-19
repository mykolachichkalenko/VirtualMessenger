package com.example.VirtualMessenger.Services;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.example.VirtualMessenger.DTOs.Message;
import com.example.VirtualMessenger.DTOs.User;
import com.example.VirtualMessenger.Services.RedisService.UserRedis;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.LocalDateTime;
import java.util.Map;

@Service
public class UsersGetService {

    private final DatabaseClient databaseClient;
    private final UserRedis userRedis;
    private final Cloudinary cloudinary;

    public UsersGetService(DatabaseClient databaseClient, UserRedis userRedis, Cloudinary cloudinary) {
        this.databaseClient = databaseClient;
        this.userRedis = userRedis;
        this.cloudinary = cloudinary;
    }

    public Flux<ServerSentEvent<User>> getUsersByName(String name, Authentication authentication) {
        String me = (String) authentication.getPrincipal();
        String sql = """
                    SELECT * FROM users
                    WHERE LOWER(phone_number) LIKE LOWER(CONCAT(:namePrefix, '%'))
                    AND phone_number != :myPhone    
                    LIMIT 10
                """;

        return databaseClient.sql(sql)
                .bind("namePrefix", name)
                .bind("myPhone", me)
                .map((row, rowMetadata) -> {
                    User user = new User();
                    user.setId(row.get("id", Long.class));
                    user.setName(row.get("name", String.class));
                    user.setPhoneNumber(row.get("phone_number", String.class));
                    user.setAvatarUrl(row.get("avatar_url", String.class));
                    user.setIsPremium(row.get("premium_subscribed", Boolean.class));
                    user.setCreatedAt(row.get("created_at", LocalDateTime.class));
                    user.setLastSeen(row.get("last_seen", String.class));

                    return ServerSentEvent.<User>builder()
                            .data(user)
                            .build();
                })
                .all()
                .switchIfEmpty(Mono.just(
                        ServerSentEvent.<User>builder()
                                .data(null)
                                .build()
                ));
    }

    public Mono<User> getUserByPhone(String phone) {
        String sql = "SELECT * FROM users WHERE phone_number = :phone";

        return userRedis.getUserByPhone(phone)
                .switchIfEmpty(databaseClient.sql(sql)
                        .bind("phone", phone)
                        .map((row, rowMetadata) -> {
                            User user = new User();
                            user.setId(row.get("id", Long.class));
                            user.setName(row.get("name", String.class));
                            user.setPhoneNumber(row.get("phone_number", String.class));
                            user.setAvatarUrl(row.get("avatar_url", String.class));
                            user.setIsPremium(row.get("premium_subscribed", Boolean.class));
                            user.setCreatedAt(row.get("created_at", LocalDateTime.class));
                            user.setLastSeen(row.get("last_seen", String.class));

                            return user;
                        })
                        .one().flatMap(user ->
                                userRedis.cacheUser(user)
                                        .thenReturn(user)))
                .switchIfEmpty(Mono.empty());
    }

    public Mono<User> getMe(Authentication authentication) {
        String phone = authentication.getPrincipal().toString();
        return getUserByPhone(phone);
    }

    public Mono<Void> setOnline(Authentication authentication) {
        String phone = authentication.getPrincipal().toString();
        String sql = "UPDATE users SET last_seen = 'online' WHERE phone_number = :phone";

        return userRedis.evictUser(phone)
                .then(databaseClient.sql(sql)
                        .bind("phone", phone)
                        .then());
    }

    public Mono<Void> changeUser(FilePart filePart, String name,String phone) {
        String sql = "UPDATE users SET name = :name, avatar_url = :avatarUrl WHERE phone_number = :phone";

        return DataBufferUtils.join(filePart.content())
                .flatMap(dataBuffer -> {
                    byte[] bytes = new byte[dataBuffer.readableByteCount()];
                    dataBuffer.read(bytes);
                    DataBufferUtils.release(dataBuffer);

                    return Mono.fromCallable(() -> {
                                Map uploadResult = cloudinary.uploader().upload(bytes, ObjectUtils.emptyMap());
                                String url = uploadResult.get("secure_url").toString();

                                User user = new User();
                                user.setAvatarUrl(url);
                                user.setName(name);
                                user.setPhoneNumber(phone);

                                return user;
                            }).subscribeOn(Schedulers.boundedElastic())
                            .flatMap(user -> {
                                return databaseClient.sql(sql)
                                        .bind("name",user.getName())
                                        .bind("avatarUrl",user.getAvatarUrl())
                                        .bind("phone",user.getPhoneNumber())
                                        .fetch()
                                        .rowsUpdated()
                                        .then(userRedis.evictUser(user.getPhoneNumber()));
                            });
                }).then();
    }
}