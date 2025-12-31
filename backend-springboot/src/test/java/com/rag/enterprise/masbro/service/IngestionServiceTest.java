package com.rag.enterprise.masbro.service;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.output.Response;
import dev.langchain4j.store.embedding.EmbeddingStore;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.util.List;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IngestionServiceTest {

    @Mock
    private EmbeddingStore<TextSegment> embeddingStore;

    @Mock
    private EmbeddingModel embeddingModel;

    @InjectMocks
    private IngestionService ingestionService;

    @Test
    void testIngestFile_Success() throws IOException{
        MockMultipartFile fakeFile = new MockMultipartFile(
                "file",
                "test.pdf",
                "application/pdf",
                "Hello World content".getBytes()
        );

        Embedding dummyEmbedding = Embedding.from(new float[]{0.1f, 0.2f, 0.3f});
        Response<List<Embedding>> fakeResponse = Response.from(List.of(dummyEmbedding));
        when(embeddingModel.embedAll(anyList())).thenReturn(fakeResponse);

        ingestionService.ingestFile(fakeFile);

        verify(embeddingModel, atLeastOnce()).embedAll(anyList());
        verify(embeddingStore, atLeastOnce()).addAll(anyList(), anyList());
    }
}
