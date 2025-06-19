package com.example.VirtualMessenger.DTOs;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChatPageShard {
    String chatId;
    String page;
    String shard;
}
