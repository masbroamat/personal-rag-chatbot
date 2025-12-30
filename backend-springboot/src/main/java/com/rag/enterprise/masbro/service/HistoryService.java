package com.rag.enterprise.masbro.service;

import com.rag.enterprise.masbro.component.OracleChatMemoryStore;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.UserMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HistoryService {

    private final OracleChatMemoryStore oracleChatMemoryStore;

    private static final String RAG_DELIMITER = "Answer using the following information:";

    public List<SimpleMessage> getChatHistory(String chatId) {
        List<ChatMessage> messages = oracleChatMemoryStore.getMessages(chatId);

        return messages.stream()
                .filter(msg -> msg instanceof UserMessage || msg instanceof AiMessage)
                .map(msg -> {
                    if (msg instanceof UserMessage) {
                        String rawText = ((UserMessage) msg).singleText();
                        if (rawText.contains(RAG_DELIMITER)) {
                            rawText = rawText.split(RAG_DELIMITER)[0].trim();
                        }
                        return new SimpleMessage(rawText, true);
                    } else {
                        return new SimpleMessage(((AiMessage) msg).text(), false);
                    }
                })
                .collect(Collectors.toList());
    }

    @Transactional
    public String deleteChatById(String chatId) {
        oracleChatMemoryStore.deleteMessages(chatId);
        return "Chat ID: " + chatId + " successfully deleted";
    }

    public record SimpleMessage(String text, boolean isUser) {}
}