package com.example.VirtualMessenger.DTOs;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
public class User {
    Long id;

    String phoneNumber;

    String name;

    String avatarUrl;

    Boolean IsPremium;

    LocalDateTime createdAt;

    String lastSeen;
}
