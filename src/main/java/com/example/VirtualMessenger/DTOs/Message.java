package com.example.VirtualMessenger.DTOs;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Message {
    Long id;
    Long chatId;
    String senderPhone;
    String receiverPhone;
    String content;
    String type;
    LocalDateTime sentAt;
    boolean isRead;
}
