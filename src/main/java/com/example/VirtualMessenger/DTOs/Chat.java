package com.example.VirtualMessenger.DTOs;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
public class Chat {
    Long id;
    String shard_of_chat;
    String first_user_phone;
    String second_user_phone;
    boolean unread_for_first_user;
    boolean unread_for_second_user;
    LocalDateTime last_updated;
}