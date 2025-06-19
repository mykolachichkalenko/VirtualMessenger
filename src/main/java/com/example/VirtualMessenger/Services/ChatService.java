package com.example.VirtualMessenger.Services;

import com.example.VirtualMessenger.DTOs.Chat;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class ChatService {

    private final DatabaseClient databaseClient;
    AtomicLong number = new AtomicLong(0);

    public ChatService(DatabaseClient databaseClient) {
        this.databaseClient = databaseClient;
    }

    public Mono<Chat> getChatByUsersPhones(String interlocutorPhone, Authentication authentication) {
        String myPhone = (String) authentication.getPrincipal();
        String sql = "SELECT * FROM chats " +
                "WHERE (first_user_phone = :user_1 AND second_user_phone = :user_2) " +
                "OR (first_user_phone = :user_2 AND second_user_phone = :user_1)";

        return databaseClient.sql(sql)
                .bind("user_1", interlocutorPhone)
                .bind("user_2", myPhone)
                .map(((row, rowMetadata) -> {
                    Chat chat = new Chat();
                    chat.setId(row.get("id", Long.class));
                    chat.setFirst_user_phone(row.get("first_user_phone", String.class));
                    chat.setSecond_user_phone(row.get("second_user_phone", String.class));
                    chat.setShard_of_chat(row.get("shard_of_chat", String.class));
                    chat.setUnread_for_first_user(Boolean.TRUE.equals(row.get("unread_for_first_user", Boolean.class)));
                    chat.setUnread_for_second_user(Boolean.TRUE.equals(row.get("unread_for_second_user", Boolean.class)));
                    chat.setLast_updated(row.get("last_updated", LocalDateTime.class));

                    return chat;
                }))
                .one()
                .switchIfEmpty(Mono.empty());
    }

    public Mono<Long> getIdChatOrGetIdChatAndCreateIdChatWithPhones(String phoneOne, String phoneTwo) {

        String sql = "SELECT * FROM chats " +
                "WHERE (first_user_phone = :user_1 AND second_user_phone = :user_2) " +
                "OR (first_user_phone = :user_2 AND second_user_phone = :user_1)";

        String sqlCreationChat = "INSERT INTO chats (shard_of_chat,first_user_phone,second_user_phone)" +
                " VALUES(:shard, :user1, :user2)";

        return databaseClient.sql(sql)
                .bind("user_1", phoneOne)
                .bind("user_2", phoneTwo)
                .map(((row, rowMetadata) -> {
                    return row.get("id", Long.class);
                }))
                .one()

                .switchIfEmpty(
                        Mono.defer(() -> {
                            long idOfTable = number.getAndIncrement() % 10;
                            String tableId = String.valueOf(idOfTable);

                            return databaseClient.sql(sqlCreationChat)
                                    .bind("shard", tableId)
                                    .bind("user1", phoneOne)
                                    .bind("user2", phoneTwo)
                                    .fetch()
                                    .rowsUpdated()
                                    .flatMap(rows -> {
                                        if (rows > 0) {
                                            return databaseClient.sql(sql)
                                                    .bind("user_1", phoneOne)
                                                    .bind("user_2", phoneTwo)
                                                    .map((row, rowMetadata) -> {
                                                        Long newId = row.get("id", Long.class);
                                                        return newId;
                                                    })
                                                    .one();
                                        } else {
                                            return Mono.error(new RuntimeException("Chat not inserted"));
                                        }
                                    })
                                    .onErrorResume(Mono::error);
                        })
                );
    }

    public Flux<ServerSentEvent<Chat>> getAllChatsByPhone(Authentication authentication) {
        String phone = authentication.getPrincipal().toString();
        String sql = "SELECT * FROM chats WHERE first_user_phone = :phone OR second_user_phone = :phone ORDER BY last_updated DESC ";

        return databaseClient.sql(sql)
                .bind("phone", phone)
                .map(((row, rowMetadata) -> {
                    Chat chat = new Chat();

                    chat.setId(row.get("id", Long.class));
                    chat.setFirst_user_phone(row.get("first_user_phone", String.class));
                    chat.setSecond_user_phone(row.get("second_user_phone", String.class));
                    chat.setShard_of_chat(row.get("shard_of_chat", String.class));
                    chat.setUnread_for_first_user(row.get("unread_for_first_user", Boolean.class));
                    chat.setUnread_for_second_user(row.get("unread_for_second_user", Boolean.class));
                    chat.setLast_updated(row.get("last_updated", LocalDateTime.class));

                    return ServerSentEvent.<Chat>builder().data(chat)
                            .build();
                }))
                .all();
    }

    public Mono<Void> deleteChatById(Long id) {
        String sql = "SELECT * FROM chats WHERE id = :id";

        return databaseClient.sql(sql)
                .bind("id", id)
                .map((row, rowMetadata) -> {
                    Chat chat = new Chat();
                    chat.setId(row.get("id", Long.class));
                    chat.setFirst_user_phone(row.get("first_user_phone", String.class));
                    chat.setSecond_user_phone(row.get("second_user_phone", String.class));
                    chat.setShard_of_chat(row.get("shard_of_chat", String.class));
                    chat.setUnread_for_first_user(row.get("unread_for_first_user", Boolean.class));
                    chat.setUnread_for_second_user(row.get("unread_for_second_user", Boolean.class));
                    chat.setLast_updated(row.get("last_updated", LocalDateTime.class));
                    return chat;
                })
                .first()
                .flatMap(chat -> {
                    String sqlDelMessages = "DELETE FROM chat_" + chat.getShard_of_chat() + " WHERE chat_id = :chat_id";
                    String sqlDelChat = "DELETE FROM chats WHERE id = :id";

                    return databaseClient.sql(sqlDelMessages)
                            .bind("chat_id", chat.getId())
                            .fetch()
                            .rowsUpdated()
                            .then(
                                    databaseClient.sql(sqlDelChat)
                                            .bind("id", chat.getId())
                                            .fetch()
                                            .rowsUpdated()
                            );
                })
                .then();
    }

    public Mono<String> getInterlocutorPhone (String myPhone,Long chatId){
        String sql = "SELECT * FROM chats WHERE id = :id";

        return databaseClient.sql(sql)
                .bind("id",chatId)
                .map((row, rowMetadata) -> {
                    String firstUserPhone = row.get("first_user_phone",String.class);
                    String secondUserPhone = row.get("second_user_phone",String.class);

                    if (firstUserPhone.equals(myPhone)){
                        return secondUserPhone;
                    }else {
                        return firstUserPhone;
                    }
                })
                .one();
    }

    public Mono<Void> setRedForMe(String myPhone,String userPhone){
        String sqlFirst = "UPDATE chats SET unread_for_first_user = false " +
                "WHERE first_user_phone = :receiverPhone AND second_user_phone = :senderPhone";

        String sqlSecond = "UPDATE chats SET unread_for_second_user = false " +
                "WHERE second_user_phone = :receiverPhone AND first_user_phone = :senderPhone";

        return databaseClient.sql(sqlFirst)
                .bind("receiverPhone", myPhone)
                .bind("senderPhone", userPhone)
                .fetch()
                .rowsUpdated()
                .then(
                        databaseClient.sql(sqlSecond)
                                .bind("receiverPhone", myPhone)
                                .bind("senderPhone", userPhone)
                                .fetch()
                                .rowsUpdated()
                                .then()
                );
    }
}