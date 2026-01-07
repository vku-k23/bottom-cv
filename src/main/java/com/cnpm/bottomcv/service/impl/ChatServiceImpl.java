package com.cnpm.bottomcv.service.impl;

import com.cnpm.bottomcv.dto.request.ChatRequest;
import com.cnpm.bottomcv.dto.request.JobSearchRequest;
import com.cnpm.bottomcv.dto.response.ChatResponse;
import com.cnpm.bottomcv.dto.response.JobResponse;
import com.cnpm.bottomcv.dto.response.ListResponse;
import com.cnpm.bottomcv.model.ChatMessage;
import com.cnpm.bottomcv.repository.ChatMessageRepository;
import com.cnpm.bottomcv.service.ChatService;
import com.cnpm.bottomcv.service.JobService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {

    private final ChatMessageRepository chatMessageRepository;
    private final JobService jobService;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(30))
            .build();

    @Value("${bottom-cv.gemini.api-key}")
    private String geminiApiKey;

    private static final String SYSTEM_PROMPT = """
        Bạn là một chatbot hỗ trợ tìm việc làm bằng tiếng Việt. Nhiệm vụ của bạn là:
        1. Trả lời ngắn gọn, rõ ràng bằng tiếng Việt (1-3 câu)
        2. Khi người dùng muốn tìm việc, hỏi tối đa 1-3 câu để thu thập thông tin: vị trí công việc, kỹ năng, mức lương, địa điểm, làm việc từ xa
        3. KHÔNG hỏi thông tin nhạy cảm như: họ tên đầy đủ, email, số điện thoại, CMND, tài khoản ngân hàng
        4. Khi đã có đủ thông tin hoặc sau 1-3 câu hỏi, gọi hàm searchJobs với các tiêu chí đã thu thập
        5. Nếu không có việc phù hợp, thông báo rõ ràng và đề xuất mở rộng tiêu chí
        6. KHÔNG bịa dữ liệu việc làm nếu không có kết quả
        
        Khi người dùng muốn tìm việc, bạn cần trích xuất các thông tin sau và trả về dưới dạng JSON:
        {
            "action": "searchJobs",
            "keyword": "vị trí công việc hoặc từ khóa",
            "location": "địa điểm",
            "minSalary": số (nếu có),
            "maxSalary": số (nếu có),
            "isRemote": true/false (nếu có)
        }
        
        Nếu chưa đủ thông tin, hỏi thêm nhưng tối đa 3 câu hỏi.
        """;

    // Gemini API endpoint - try different models/versions if one fails
    // Priority: v1 with gemini-1.5-flash > v1beta with gemini-pro
    private static final String[] GEMINI_API_URLS = {
        "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent",
        "https://generativelanguage.googleapis.com/v1beta/models/gemini-pro:generateContent",
        "https://generativelanguage.googleapis.com/v1/models/gemini-pro:generateContent"
    };

    @Override
    @Transactional
    public ChatResponse sendMessage(ChatRequest request) {
        try {
            // Check API key
            if (geminiApiKey == null || geminiApiKey.isEmpty()) {
                log.error("Gemini API key is not configured");
                return ChatResponse.builder()
                        .reply("Cấu hình API chưa đúng. Vui lòng liên hệ quản trị viên.")
                        .conversationId(request.getConversationId() != null ? request.getConversationId() : UUID.randomUUID().toString())
                        .build();
            }

            // Generate or use existing conversationId
            String conversationId = request.getConversationId();
            if (conversationId == null || conversationId.isEmpty()) {
                conversationId = UUID.randomUUID().toString();
            }

            // Retrieve conversation history (last 10 messages for context)
            List<ChatMessage> history = chatMessageRepository.findByConversationIdOrderByCreatedAtAsc(conversationId);
            List<ChatMessage> recentHistory = history.size() > 10 
                ? history.subList(history.size() - 10, history.size())
                : history;

            // Store user message
            ChatMessage userMessage = ChatMessage.builder()
                    .conversationId(conversationId)
                    .role("USER")
                    .message(request.getMessage())
                    .build();
            chatMessageRepository.save(userMessage);

            // Build request for Gemini API
            String requestBody = buildGeminiRequest(SYSTEM_PROMPT, recentHistory, request.getMessage());
            
            // Call Gemini API
            String aiResponse = callGeminiAPI(requestBody);

            // Check if response contains job search request
            if (aiResponse.contains("searchJobs") || aiResponse.contains("\"action\":\"searchJobs\"")) {
                aiResponse = handleJobSearch(aiResponse, conversationId);
            }

            // Store AI response
            ChatMessage aiMessage = ChatMessage.builder()
                    .conversationId(conversationId)
                    .role("AI")
                    .message(aiResponse)
                    .build();
            chatMessageRepository.save(aiMessage);

            return ChatResponse.builder()
                    .reply(aiResponse)
                    .conversationId(conversationId)
                    .build();

        } catch (Exception e) {
            log.error("Error processing chat message", e);
            // Don't expose API key in error messages
            String errorMessage = "Xin lỗi, đã có lỗi xảy ra. Vui lòng thử lại sau.";
            if (e.getMessage() != null && (e.getMessage().contains("API key") || e.getMessage().contains("401"))) {
                errorMessage = "Cấu hình API chưa đúng. Vui lòng liên hệ quản trị viên.";
            } else if (e.getMessage() != null && (e.getMessage().contains("429") || e.getMessage().contains("quota"))) {
                errorMessage = "Xin lỗi, tôi đã vượt quá hạn mức sử dụng. Vui lòng thử lại sau hoặc liên hệ quản trị viên.";
            }
            return ChatResponse.builder()
                    .reply(errorMessage)
                    .conversationId(request.getConversationId() != null ? request.getConversationId() : UUID.randomUUID().toString())
                    .build();
        }
    }

    private String buildGeminiRequest(String systemPrompt, List<ChatMessage> history, String currentMessage) {
        try {
            Map<String, Object> request = new HashMap<>();
            List<Map<String, Object>> contents = new ArrayList<>();

            // Build conversation history
            // First, add system instruction as the first user message with context
            StringBuilder conversationContext = new StringBuilder(systemPrompt);
            conversationContext.append("\n\nLịch sử hội thoại:\n");
            
            for (ChatMessage msg : history) {
                if ("USER".equals(msg.getRole())) {
                    conversationContext.append("Người dùng: ").append(msg.getMessage()).append("\n");
                } else {
                    conversationContext.append("Bạn: ").append(msg.getMessage()).append("\n");
                }
            }
            
            conversationContext.append("Người dùng: ").append(currentMessage);

            // Create content with parts
            Map<String, Object> content = new HashMap<>();
            List<Map<String, String>> parts = new ArrayList<>();
            Map<String, String> part = new HashMap<>();
            part.put("text", conversationContext.toString());
            parts.add(part);
            content.put("parts", parts);
            contents.add(content);

            request.put("contents", contents);
            
            // Add generation config
            Map<String, Object> generationConfig = new HashMap<>();
            generationConfig.put("temperature", 0.7);
            generationConfig.put("maxOutputTokens", 500);
            request.put("generationConfig", generationConfig);

            return objectMapper.writeValueAsString(request);
        } catch (Exception e) {
            log.error("Error building Gemini request", e);
            throw new RuntimeException("Failed to build Gemini request", e);
        }
    }

    private String callGeminiAPI(String requestBody) {
        Exception lastException = null;
        
        // Try each endpoint until one works
        for (String baseUrl : GEMINI_API_URLS) {
            try {
                String url = baseUrl + "?key=" + geminiApiKey;
                
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                        .timeout(Duration.ofSeconds(30))
                        .build();

                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() == 200) {
                    // Success! Parse and return response
                    return parseGeminiResponse(response.body());
                } else if (response.statusCode() == 404) {
                    // Try next endpoint
                    log.warn("Gemini API endpoint not found (404): {}, trying next...", baseUrl);
                    lastException = new RuntimeException("404 - " + baseUrl);
                    continue;
                } else {
                    // Other error, log and try next
                    String errorBody = response.body();
                    log.warn("Gemini API error: Status {}, URL: {}, Body: {}", response.statusCode(), url, errorBody);
                    lastException = new RuntimeException("Gemini API returned status: " + response.statusCode() + " - " + errorBody);
                    continue;
                }
            } catch (Exception e) {
                log.warn("Error calling Gemini API endpoint {}: {}", baseUrl, e.getMessage());
                lastException = e;
                continue;
            }
        }
        
        // All endpoints failed
        log.error("All Gemini API endpoints failed. Last error: {}", lastException != null ? lastException.getMessage() : "Unknown");
        if (lastException != null && lastException.getMessage() != null) {
            if (lastException.getMessage().contains("status: 429")) {
                throw new RuntimeException("429", lastException);
            } else if (lastException.getMessage().contains("status: 401")) {
                throw new RuntimeException("401", lastException);
            }
        }
        throw new RuntimeException("Failed to call Gemini API with all available endpoints. Please check your API key and model access.", lastException);
    }
    
    private String parseGeminiResponse(String responseBody) {
        try {
            // Parse response
            JsonNode jsonResponse = objectMapper.readTree(responseBody);
            JsonNode candidates = jsonResponse.get("candidates");
            
            if (candidates == null || !candidates.isArray() || candidates.size() == 0) {
                log.error("No candidates in Gemini response: {}", responseBody);
                throw new RuntimeException("No candidates in Gemini response");
            }

            JsonNode content = candidates.get(0).get("content");
            if (content == null) {
                log.error("No content in Gemini response: {}", responseBody);
                throw new RuntimeException("No content in Gemini response");
            }

            JsonNode parts = content.get("parts");
            if (parts == null || !parts.isArray() || parts.size() == 0) {
                log.error("No parts in Gemini response: {}", responseBody);
                throw new RuntimeException("No parts in Gemini response");
            }

            String text = parts.get(0).get("text").asText();
            return text;
        } catch (Exception e) {
            log.error("Error parsing Gemini response", e);
            throw new RuntimeException("Failed to parse Gemini API response", e);
        }
    }

    private String handleJobSearch(String aiResponse, String conversationId) {
        try {
            // Try to extract JSON from response
            JobSearchRequest searchRequest = extractJobSearchCriteria(aiResponse);
            
            // Set default pagination
            searchRequest.setPage(0);
            searchRequest.setSize(7); // Get up to 7 jobs
            
            // Call job service
            ListResponse<JobResponse> jobsResponse = jobService.getAllJobs(searchRequest);
            List<JobResponse> jobs = jobsResponse.getData();

            if (jobs == null || jobs.isEmpty()) {
                return "Xin lỗi, tôi không tìm thấy việc làm nào phù hợp với tiêu chí của bạn. " +
                       "Bạn có thể thử mở rộng tiêu chí tìm kiếm hoặc thử lại với từ khóa khác.";
            }

            // Format job results (limit to 7)
            int jobCount = Math.min(jobs.size(), 7);
            StringBuilder response = new StringBuilder();
            response.append("Tôi đã tìm thấy ").append(jobCount).append(" việc làm phù hợp:\n\n");

            for (int i = 0; i < jobCount; i++) {
                JobResponse job = jobs.get(i);
                response.append((i + 1)).append(". ").append(job.getTitle()).append("\n");
                response.append("   Công ty: ").append(job.getCompany() != null ? job.getCompany().getName() : "N/A").append("\n");
                response.append("   Địa điểm: ").append(job.getLocation() != null ? job.getLocation() : "N/A").append("\n");
                if (job.getSalary() != null) {
                    response.append("   Lương: ").append(formatSalary(job.getSalary())).append("\n");
                }
                response.append("   Link: ").append("http://localhost:3000/jobs/").append(job.getId()).append("\n");
                response.append("   Lý do: Phù hợp với tiêu chí tìm kiếm của bạn\n\n");
            }

            return response.toString();

        } catch (Exception e) {
            log.error("Error handling job search", e);
            return "Xin lỗi, đã có lỗi khi tìm kiếm việc làm. Vui lòng thử lại sau.";
        }
    }

    private JobSearchRequest extractJobSearchCriteria(String response) {
        JobSearchRequest request = new JobSearchRequest();
        
        try {
            // Try to parse JSON from response
            Pattern jsonPattern = Pattern.compile("\\{[^}]*\"action\"[^}]*\\}", Pattern.DOTALL);
            Matcher matcher = jsonPattern.matcher(response);
            
            if (matcher.find()) {
                String jsonStr = matcher.group();
                // Simple JSON parsing (for MVP, can be improved)
                extractField(jsonStr, "keyword", request::setKeyword);
                extractField(jsonStr, "location", request::setLocation);
                extractSalary(jsonStr, request);
                extractRemote(jsonStr, request);
            } else {
                // Fallback: extract from natural language
                extractFromText(response, request);
            }
        } catch (Exception e) {
            log.warn("Could not extract job search criteria from JSON, using text extraction", e);
            extractFromText(response, request);
        }
        
        return request;
    }

    private void extractField(String json, String fieldName, java.util.function.Consumer<String> setter) {
        Pattern pattern = Pattern.compile("\"" + fieldName + "\"\\s*:\\s*\"([^\"]+)\"");
        Matcher matcher = pattern.matcher(json);
        if (matcher.find()) {
            setter.accept(matcher.group(1));
        }
    }

    private void extractSalary(String json, JobSearchRequest request) {
        Pattern minPattern = Pattern.compile("\"minSalary\"\\s*:\\s*(\\d+)");
        Pattern maxPattern = Pattern.compile("\"maxSalary\"\\s*:\\s*(\\d+)");
        
        Matcher minMatcher = minPattern.matcher(json);
        if (minMatcher.find()) {
            request.setMinSalary(Double.parseDouble(minMatcher.group(1)));
        }
        
        Matcher maxMatcher = maxPattern.matcher(json);
        if (maxMatcher.find()) {
            request.setMaxSalary(Double.parseDouble(maxMatcher.group(1)));
        }
    }

    private void extractRemote(String json, JobSearchRequest request) {
        Pattern pattern = Pattern.compile("\"isRemote\"\\s*:\\s*(true|false)");
        Matcher matcher = pattern.matcher(json);
        if (matcher.find() && "true".equals(matcher.group(1))) {
            // Note: JobSearchRequest doesn't have isRemote field, 
            // but we can use location as "Remote" or handle in job type
            request.setLocation("Remote");
        }
    }

    private void extractFromText(String text, JobSearchRequest request) {
        // Simple keyword extraction - look for common job-related phrases
        if (text.toLowerCase().contains("lập trình") || text.toLowerCase().contains("developer")) {
            request.setKeyword("lập trình");
        } else if (text.toLowerCase().contains("marketing")) {
            request.setKeyword("marketing");
        } else if (text.toLowerCase().contains("bán hàng") || text.toLowerCase().contains("sales")) {
            request.setKeyword("bán hàng");
        }
        
        // Extract location
        String[] locations = {"Hà Nội", "Hồ Chí Minh", "Đà Nẵng", "Remote", "từ xa"};
        for (String loc : locations) {
            if (text.contains(loc)) {
                request.setLocation(loc);
                break;
            }
        }
    }

    private String formatSalary(Double salary) {
        if (salary == null) return "Thỏa thuận";
        if (salary >= 1000000) {
            return String.format("%.0f triệu", salary / 1000000);
        }
        return String.format("%.0f", salary);
    }
}
