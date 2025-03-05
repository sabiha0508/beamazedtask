package com.youtube.ai.intro.controller;

import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import java.util.*;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*") //http://localhost:4200") // ✅ Allow Angular to call this API
public class IntroController {

    private static final String OPENAI_API_KEY = ""; // Replace with your OpenAI API Key

    @PostMapping("/generate-intro")
    public Map<String, String> generateIntro(@RequestBody Map<String, String> request) {
        String script = request.get("script");
        String intro = callOpenAI(script);
        Map<String, String> response = new HashMap<>();
        response.put("intro", intro);
        return response;
    }

    private String callOpenAI(String script) {
        String openAiUrl = "https://api.openai.com/v1/chat/completions";

        // Prepare request body
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "gpt-3.5-turbo"); // Use GPT-4 model (or whichever is needed)
        requestBody.put("messages", List.of(
                Map.of("role", "system", "content", "You are an expert YouTube script writer."),
                Map.of("role", "user", "content", "Generate a catchy YouTube intro for this script: " + script)
        ));
        requestBody.put("max_tokens", 100);

        // Correctly set HTTP headers
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + OPENAI_API_KEY); // Correct header format
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Create request entity
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        RestTemplate restTemplate = new RestTemplate();
        try {
            ResponseEntity<Map> response = restTemplate.exchange(openAiUrl, HttpMethod.POST, entity, Map.class);

            // Parse response
            if (response.getBody() != null && response.getBody().containsKey("choices")) {
                List<Map<String, Object>> choices = (List<Map<String, Object>>) response.getBody().get("choices");
                if (!choices.isEmpty() && choices.get(0).containsKey("message")) {
                    Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
                    return (String) message.get("content");
                }
            }

            return "Error generating intro.";

        } catch (HttpClientErrorException e) {
            // Handle error cases like 429 or 500
            if (e.getStatusCode() == HttpStatus.TOO_MANY_REQUESTS) {
                return "Rate limit exceeded. Please wait and try again later.";
            } else if (e.getStatusCode() == HttpStatus.INTERNAL_SERVER_ERROR) {
                return "Internal server error occurred while processing the request.";
            } else {
                return "Error: " + e.getMessage();
            }
        } catch (Exception e) {
            // General catch-all for unexpected errors
            return "Unexpected error occurred: " + e.getMessage();
        }
    }


}
