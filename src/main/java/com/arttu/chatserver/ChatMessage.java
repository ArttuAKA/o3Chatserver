package com.arttu.chatserver;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

public class ChatMessage {
    
    LocalDateTime sent;
    String nick;
    String message;

    
    public ChatMessage(String user, String message2, LocalDateTime sent) {
        this.nick=user;
        this.message=message2;
        this.sent=sent;
    }

    

    long dateAsInt() {
        return sent.toInstant(ZoneOffset.UTC).toEpochMilli();
    }

    void setSent(long epoch) {
        sent = LocalDateTime.ofInstant(Instant.ofEpochMilli(epoch), ZoneOffset.UTC);
    }
}
