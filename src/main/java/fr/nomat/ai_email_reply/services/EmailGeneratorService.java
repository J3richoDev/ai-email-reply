package fr.nomat.ai_email_reply.services;

import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import fr.nomat.ai_email_reply.dto.EmailRequestDTO;

@Service
public class EmailGeneratorService {

    private final WebClient webClient;

    public EmailGeneratorService(WebClient webClient) {
        this.webClient = webClient;
    }

    @Value("${gemini.api.url}")
    private String geminiApiUrl;
    @Value("${gemini.api.key}")
    private String geminiApiKey;

    public String generateEmailReply(EmailRequestDTO emailRequestDTO) {
        // Build prompt
        String prompt = buildPrompt(emailRequestDTO);

        // Craft a request
        Map<String, Object> requestBody = Map.of(
                "contents", new Object[] {
                        Map.of("parts", new Object[] {
                                Map.of("text", prompt)
                        })
                });

        // Do request and get response
        String response = webClient.post().uri(geminiApiUrl + geminiApiKey)
                .header("Content-Type", "application/json")
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(String.class)
                .block();

        // return response
        return extractResponseContent(response);
    }

    private String buildPrompt(EmailRequestDTO emailRequestDTO) {
        StringBuilder prompt = new StringBuilder();

        prompt.append("Generate a professional email reply for the following email content. P");
        if (emailRequestDTO.getTone() != null && !emailRequestDTO.getTone().isEmpty()) {
            prompt.append("Use a ").append(emailRequestDTO.getTone()).append((" tone."));
        }
        prompt.append("\nOriginal email: \n").append(emailRequestDTO.getEmailContent());

        return prompt.toString();

    }

    private String extractResponseContent(String response) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode rootNode = mapper.readTree(response);
            return rootNode.path("candidates").get(0).path("content").path("parts").get(0).path("text").asText();
        } catch (Exception e) {
            return "Error processing request: " + e.getMessage();
        }
    }
}
