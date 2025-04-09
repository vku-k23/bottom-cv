package com.cnpm.bottomcv.controller;

import com.google.cloud.vertexai.VertexAI;
import com.google.cloud.vertexai.api.GenerateContentResponse;
import com.google.cloud.vertexai.generativemodel.GenerativeModel;
import com.google.cloud.vertexai.generativemodel.Part;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api/chatbot")
public class ChatbotController {

    @Value("${gemini.api-key}")
    private String geminiApiKey;

    @PostMapping("/ask")
    public ResponseEntity<?> askQuestion(@RequestBody Map<String, String> request) {
        String question = request.get("question");
        if (question == null || question.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Question cannot be empty"));
        }
        try {
            String answer = getAnswerFromGemini(question);
            return ResponseEntity.ok(Map.of("answer", answer));
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Error communicating with Gemini API: " + e.getMessage()));
        }
    }

    private String getAnswerFromGemini(String question) throws IOException {
        try (VertexAI vertexAI = new VertexAI("vku-test", "asia-southeast1", geminiApiKey)) {
            GenerativeModel model = new GenerativeModel("gemini-1.5-flash-001", vertexAI);
            GenerateContentResponse response = model.generateContent(Part.from(question));
            return response.getCandidates().get(0).getContent().getParts().get(0).getText();
        }
    }
}