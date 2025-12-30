package com.rag.enterprise.masbro.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "CHAT_MEMORY")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatMemoryEntity {

    @Id
    @Column(name = "CHAT_ID")
    private String chatId;

    @Lob
    @Column(name = "MESSAGES")
    private String messages;
}