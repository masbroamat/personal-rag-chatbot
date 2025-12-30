package com.rag.enterprise.masbro.component;

import com.rag.enterprise.masbro.entity.ChatMemoryEntity;
import com.rag.enterprise.masbro.repository.ChatMemoryRepository;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.ChatMessageDeserializer;
import dev.langchain4j.data.message.ChatMessageSerializer;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class OracleChatMemoryStore implements ChatMemoryStore {

    private final ChatMemoryRepository chatMemoryRepository;

    @Override
    public List<ChatMessage> getMessages(Object memoryId) {
        return chatMemoryRepository.findById(memoryId.toString())
                .map(entity -> {
                    return ChatMessageDeserializer.messagesFromJson(entity.getMessages());
                })
                .orElse(new ArrayList<>());
    }

    @Override
    public void updateMessages(Object memoryId, List<ChatMessage> messages) {
        String json = ChatMessageSerializer.messagesToJson(messages);
        ChatMemoryEntity entity = new ChatMemoryEntity(memoryId.toString(), json);
        chatMemoryRepository.save(entity);
    }

    @Override
    public void deleteMessages(Object memoryId) {
        chatMemoryRepository.deleteByChatId(memoryId.toString());
    }
}