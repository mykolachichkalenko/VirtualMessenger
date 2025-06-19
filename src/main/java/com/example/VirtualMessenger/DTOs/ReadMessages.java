package com.example.VirtualMessenger.DTOs;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReadMessages {
    String shard;
    String chatId;
    String phoneUser;
}
