package com.rag.enterprise.masbro.service;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.loader.FileSystemDocumentLoader;
import dev.langchain4j.data.document.parser.apache.tika.ApacheTikaDocumentParser;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Service
@Slf4j
@RequiredArgsConstructor
public class IngestionService {

    private final EmbeddingStore<TextSegment> embeddingStore;
    private final EmbeddingModel embeddingModel;

    public void ingestText(String text) {
        Document document = Document.from(text);

        EmbeddingStoreIngestor ingestor = EmbeddingStoreIngestor.builder()
                .embeddingStore(embeddingStore)
                .embeddingModel(embeddingModel)
                .build();

        ingestor.ingest(document);
    }

    public void ingestFile(MultipartFile file) throws IOException {
        Path tempPath = Files.createTempFile("rag_upload_", file.getOriginalFilename());
        file.transferTo(tempPath.toFile());

        try {
            log.info("Parsing file: {}", file.getOriginalFilename());

            Document document = FileSystemDocumentLoader.loadDocument(
                    tempPath,
                    new ApacheTikaDocumentParser()
            );

            ingestDocument(document);
        } finally {
            Files.deleteIfExists(tempPath);
        }
    }

    private void ingestDocument(Document document) {
        log.info("Start Ingestion: Splitting and Embedding...");

        EmbeddingStoreIngestor ingestor = EmbeddingStoreIngestor.builder()
                .embeddingStore(embeddingStore)
                .embeddingModel(embeddingModel)
                // The '200' is the overlap, so context isn't lost between cuts.
                .documentSplitter(DocumentSplitters.recursive(1000, 200))
                .build();

        ingestor.ingest(document);
        log.info("Ingestion Complete!");
    }
}