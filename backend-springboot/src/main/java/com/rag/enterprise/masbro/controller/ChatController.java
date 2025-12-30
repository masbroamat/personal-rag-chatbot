package com.rag.enterprise.masbro.controller;

import com.rag.enterprise.masbro.service.AgentService;
import com.rag.enterprise.masbro.service.HistoryService;
import com.rag.enterprise.masbro.service.IngestionService;
import dev.langchain4j.model.chat.ChatLanguageModel;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/rag/v1")
public class ChatController {

    private final ChatLanguageModel chatModel;
    private final IngestionService ingestionService;
    private final AgentService agentService;
    private final HistoryService historyService;

    @PostMapping(value = "/ingest/file", consumes = "multipart/form-data")
    public String ingestFile(@RequestParam("file") MultipartFile file) {
        try {
            ingestionService.ingestFile(file);
            return "Successfully stored file: " + file.getOriginalFilename();
        } catch (Exception e) {
            return "Failed to ingest file: " + e.getMessage();
        }
    }

    @PostMapping("/ingest/text")
    public String ingestText(@RequestBody String text) {
        ingestionService.ingestText(text);
        return "Successfully stored text snippet.";
    }

    @GetMapping("/chat")
    public String chat(@RequestParam String question, @RequestParam String chatId) {
        return agentService.chat(chatId, question);
    }

    @GetMapping("/history")
    public List<HistoryService.SimpleMessage> getHistory(@RequestParam String chatId) {
        return historyService.getChatHistory(chatId);
    }

    @DeleteMapping("/deleteChat")
    public ResponseEntity<String> deleteChat(@RequestParam String chatId) {
        try {
            historyService.deleteChatById(chatId);
            return ResponseEntity.ok("Chat ID: " + chatId + " successfully deleted");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error deleting chat: " + e.getMessage());
        }
    }
}