package com.rag.enterprise.masbro.config;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.memory.chat.ChatMemoryProvider;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.ollama.OllamaChatModel;
import dev.langchain4j.model.ollama.OllamaEmbeddingModel;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.oracle.CreateOption;
import dev.langchain4j.store.embedding.oracle.EmbeddingTable;
import dev.langchain4j.store.embedding.oracle.OracleEmbeddingStore;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.rag.enterprise.masbro.component.OracleChatMemoryStore;
import javax.sql.DataSource;

import java.time.Duration;

@Configuration
@Slf4j
public class AiConfig {

    @Value("${spring.ai.ollama.chat.model}")
    private String activeModelName;

    @PostConstruct
    public void logActiveModel() {
        log.info("============================================");
        log.info("MASBRO AI IS RUNNING ON MODEL: {}", activeModelName);
        log.info("============================================");
    }

    @Bean
    public ChatLanguageModel chatLanguageModel(
            @Value("${spring.ai.ollama.base-url:http://localhost:11434}") String baseUrl,
            @Value("${spring.ai.ollama.chat.options.num-ctx:8192}") int contextSize
    ) {
        return OllamaChatModel.builder()
                .baseUrl(baseUrl)
                .modelName(activeModelName)
                .numCtx(contextSize)
                .timeout(Duration.ofSeconds(300))
                .build();
    }

    @Bean
    public EmbeddingModel embeddingModel() {
        return OllamaEmbeddingModel.builder()
                .baseUrl("http://localhost:11434")
                .modelName("nomic-embed-text")
                .timeout(Duration.ofSeconds(180))
                .build();
    }

    @Bean
    public EmbeddingStore<TextSegment> embeddingStore(DataSource dataSource) {
        return OracleEmbeddingStore.builder()
                .dataSource(dataSource)
                .embeddingTable(EmbeddingTable.builder()
                        .name("DOCUMENT_EMBEDDINGS")
                        .idColumn("ID")
                        .textColumn("CONTENT")
                        .embeddingColumn("EMBEDDING")
                        .metadataColumn("METADATA")
                        .createOption(CreateOption.CREATE_IF_NOT_EXISTS)
                        .build())
                .build();
    }

    @Bean
    public ContentRetriever contentRetriever(EmbeddingStore<TextSegment> embeddingStore, EmbeddingModel embeddingModel) {
        return EmbeddingStoreContentRetriever.builder()
                .embeddingStore(embeddingStore)
                .embeddingModel(embeddingModel)
                .maxResults(10)
                .minScore(0.6)
                .build();
    }

    @Bean
    public ChatMemoryProvider chatMemoryProvider(OracleChatMemoryStore oracleChatMemoryStore) {
        return memoryId -> MessageWindowChatMemory.builder()
                .id(memoryId)
                .chatMemoryStore(oracleChatMemoryStore)
                .maxMessages(20)
                .build();
    }
}