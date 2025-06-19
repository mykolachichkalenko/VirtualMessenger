package com.example.VirtualMessenger.Services;

import com.example.VirtualMessenger.DTOs.Message;
import com.example.VirtualMessenger.Services.WebsocketService.MessageDelete;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.Objects;

@Service
public class MessageService {

    private final DatabaseClient databaseClient;

    public MessageService(DatabaseClient databaseClient) {
        this.databaseClient = databaseClient;
    }

    public Mono<Void> addMessage(Message message) {

        return getShardByChatId(message.getChatId())
                .flatMap(shard -> {
                    return databaseClient.sql("INSERT INTO chat_" + String.valueOf(shard) + " (chat_id,sender_phone,receiver_phone,content,type) VALUES(:chatId,:senderPhone,:receiverPhone,:content,:type)")
                            .bind("chatId", message.getChatId())
                            .bind("senderPhone", message.getSenderPhone())
                            .bind("receiverPhone", message.getReceiverPhone())
                            .bind("content", message.getContent())
                            .bind("type", message.getType())
                            .fetch()
                            .rowsUpdated()
                            .then();
                });
    }

    public Mono<Void> addMessageAndSetUnread(Message message) {

        return getShardByChatId(message.getChatId())
                .flatMap(shard -> {
                    return databaseClient.sql("INSERT INTO chat_" + String.valueOf(shard) + " (chat_id,sender_phone,receiver_phone,content,type) VALUES(:chatId,:senderPhone,:receiverPhone,:content,:type)")
                            .bind("chatId", message.getChatId())
                            .bind("senderPhone", message.getSenderPhone())
                            .bind("receiverPhone", message.getReceiverPhone())
                            .bind("content", message.getContent())
                            .bind("type", message.getType())
                            .fetch()
                            .rowsUpdated()
                            .then(setUnReadForFirstUser(message.getReceiverPhone(),message.getSenderPhone()));
                });
    }

    public Mono<Void> setUnReadForFirstUser(String unReadUserPhone, String senderPhone) {
        String sqlFirst = "UPDATE chats SET unread_for_first_user = true " +
                "WHERE first_user_phone = :receiverPhone AND second_user_phone = :senderPhone";

        String sqlSecond = "UPDATE chats SET unread_for_second_user = true " +
                "WHERE second_user_phone = :receiverPhone AND first_user_phone = :senderPhone";

        return databaseClient.sql(sqlFirst)
                .bind("receiverPhone", unReadUserPhone)
                .bind("senderPhone", senderPhone)
                .fetch()
                .rowsUpdated()
                .then(
                        databaseClient.sql(sqlSecond)
                                .bind("receiverPhone", unReadUserPhone)
                                .bind("senderPhone", senderPhone)
                                .fetch()
                                .rowsUpdated()
                                .then()
                );
    }

    public Mono<Long> getShardByChatId(Long chatId) {
        String sql = "SELECT * FROM chats WHERE id = :chatId";

        return databaseClient.sql(sql)
                .bind("chatId", chatId)
                .map((row, rowMetadata) -> {
                    return Long.parseLong(Objects.requireNonNull(row.get("shard_of_chat", String.class)));
                })
                .one();
    }

    public Flux<Message> getMessagesByPage(long chatId, String shard, int page) {
        String table = "chat_" + shard;
        int limit = 30;
        int offset = page * limit;
        String sql = "SELECT * FROM " + table + " WHERE chat_id = :chatId ORDER BY id DESC LIMIT " + limit + " OFFSET " + offset;

        return databaseClient.sql(sql)
                .bind("chatId", chatId)
                .map((row, rowMetadata) -> {
                    Message message = new Message();
                    message.setId(row.get("id", Long.class));
                    message.setType(row.get("type", String.class));
                    message.setChatId(row.get("chat_id", Long.class));
                    message.setRead(Boolean.TRUE.equals(row.get("is_read", Boolean.class)));
                    message.setContent(row.get("content", String.class));
                    message.setSenderPhone(row.get("sender_phone", String.class));
                    message.setReceiverPhone(row.get("receiver_phone", String.class));
                    message.setSentAt(row.get("sent_at", LocalDateTime.class));


                    return message;
                })
                .all();
    }

    public Mono<Void> markAsRed(String shard, Long chatId, String userPhone) {
        String table = "chat_" + shard;
        String sql = "UPDATE " + table + " SET is_read = true WHERE chat_id = :chatId AND sender_phone = :senderPhone AND is_read = false";

        return databaseClient.sql(sql)
                .bind("chatId", chatId)
                .bind("senderPhone", userPhone)
                .fetch()
                .rowsUpdated()
                .then();
    }

    public Mono<ServerSentEvent<Long>> getLastUnMarked(Long chatId, String shard, String phoneUser) {
        String table = "chat_" + shard;
        String sql = "SELECT MIN(id) as min_unread_id FROM " + table + " WHERE chat_id = :chatId AND sender_phone != :phoneUser AND is_read = false";

        return databaseClient.sql(sql)
                .bind("chatId",chatId)
                .bind("phoneUser",phoneUser)
                .map(((row, rowMetadata) -> {
                    Long id = row.get("min_unread_id", Long.class);
                    if (id==null){
                        id = 0L;
                    }
                    return ServerSentEvent.<Long>builder()
                            .data(id)
                            .build();
                }))
                .one();
    }

    public Mono<Void> deleteMessageSafely (Message message,String shard){
        String sql =  "DELETE FROM chat_"+shard+" WHERE id = (" +
                " SELECT id FROM (" +
                "  SELECT id FROM (" +
                "    SELECT id FROM chat_"+shard+" WHERE id = :givenId AND chat_id = :chatId AND sender_phone = :senderPhone AND receiver_phone = :receiverPhone " +
                "    UNION ALL " +
                "    SELECT id FROM chat_"+shard+" WHERE id > :givenId AND chat_id = :chatId AND sender_phone = :senderPhone AND receiver_phone = :receiverPhone ORDER BY id LIMIT 1" +
                "  ) AS temp " +
                "  ORDER BY id LIMIT 1" +
                ") AS sub " +
                ")";

        return databaseClient.sql(sql)
                .bind("givenId",message.getId())
                .bind("senderPhone",message.getSenderPhone())
                .bind("receiverPhone",message.getReceiverPhone())
                .bind("chatId",message.getChatId())
                .fetch()
                .rowsUpdated()
                .then();
    }

    public Mono<Void> updateMessage(Message message,String shard){
        String sql = "UPDATE chat_" + shard + " SET content = :newContent WHERE id = (" +
                "  SELECT id FROM (" +
                "    SELECT id FROM (" +
                "      SELECT id FROM chat_" + shard + " WHERE id = :givenId AND chat_id = :chatId AND sender_phone = :senderPhone AND receiver_phone = :receiverPhone " +
                "      UNION ALL " +
                "      SELECT id FROM chat_" + shard + " WHERE id > :givenId AND chat_id = :chatId AND sender_phone = :senderPhone AND receiver_phone = :receiverPhone ORDER BY id LIMIT 1" +
                "    ) AS temp " +
                "    ORDER BY id LIMIT 1" +
                "  ) AS sub" +
                ")";

        return databaseClient.sql(sql)
                .bind("givenId",message.getId())
                .bind("senderPhone",message.getSenderPhone())
                .bind("receiverPhone",message.getReceiverPhone())
                .bind("chatId",message.getChatId())
                .bind("newContent",message.getContent())
                .fetch()
                .rowsUpdated()
                .then();
    }
}
