package com.example.VirtualMessenger.DTOs;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class TypingMessage {
    private String fromUserPhone;

    private String toUserPhone;

    private boolean type;
}