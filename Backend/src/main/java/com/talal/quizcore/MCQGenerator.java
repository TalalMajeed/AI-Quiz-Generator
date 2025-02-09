package com.talal.quizcore;

import org.json.JSONArray;
import org.json.JSONObject;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Value;

import java.io.IOException;

public class MCQGenerator {
    @Value("${openai.api.key}")
    private static String openAiApiKey;

    private static JSONArray MCQs = new JSONArray();

    public static JSONObject generateMCQs(int amount, int difficulty, String subject, String details) {
        String userPrompt = getUserPrompt(amount, difficulty, subject, details);
        String systemPrompt = getSystemPrompt();
        return getGPTResponse(systemPrompt, userPrompt);
    }

    private static String getUserPrompt(int amount, int difficulty, String subject, String details) {
        return String.format(
                "Generate %d multiple-choice questions for Students aged %d in %s. %s. Provide the questions in the following format:\n[\n    {\n        \"Q\": \"This is the Question in sentence form.\",\n        \"O\": [\"A1\", \"A2\", \"A3\", \"A4\"],\n        \"A\": 0\n    },\n    {\n        \"Q\": \"This is the Question in sentence form.\",\n        \"O\": [\"A1\", \"A2\", \"A3\", \"A4\"],\n        \"A\": 0\n    },\n    and so on\n]",
                amount, difficulty, subject, details
        );
    }

    private static String getSystemPrompt() {
        return """
            format
            You are a Question Bank Generator. Your job is to generate multiple-choice questions (MCQs) based on the user's specifications. Each question should have 4 options, and one correct answer.
            Your responses should be in JSON format.
            Example relevant prompts include: 
            'Generate 10 MCQs for 12th grade Physics on Harmonic Oscillations.'
            'Create 5 multiple-choice questions for university-level biology on cellular respiration.'
            """;
    }

    private static JSONObject getGPTResponse(String systemPrompt, String userInput) {
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpPost post = new HttpPost("https://api.openai.com/v1/chat/completions");
            post.setHeader("Content-type", "application/json");
            post.setHeader("Authorization", "Bearer " + openAiApiKey);

            JSONObject body = new JSONObject();
            body.put("model", "gpt-3.5-turbo");
            JSONArray messages = new JSONArray();
            messages.put(new JSONObject().put("role", "system").put("content", systemPrompt));
            messages.put(new JSONObject().put("role", "user").put("content", userInput));
            body.put("messages", messages);

            post.setEntity(new StringEntity(body.toString()));

            String response = EntityUtils.toString(client.execute(post).getEntity());

            JSONObject jsonResponse = new JSONObject(response);
            JSONArray choices = jsonResponse.getJSONArray("choices");
            String message = choices.getJSONObject(0).getJSONObject("message").getString("content");

            JSONObject responseObj = new JSONObject();
            responseObj.put("MCQs", message);
            responseObj.put("status", 200);
            responseObj.put("message", "Successfully Generated MCQs");

            return responseObj;
        } catch (IOException e) {
            e.printStackTrace();
            return new JSONObject().put("status", 500).put("error", "Unexpected error: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return new JSONObject().put("status", 500).put("error", "Unexpected error: " + e.getMessage());
        }
    }
}
