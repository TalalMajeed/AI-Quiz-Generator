package com.talal.quizcore;
import org.json.JSONArray;
import org.json.JSONObject;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import java.io.IOException;

public class MCQGenerator {
    private static JSONArray MCQs = new JSONArray();
    public static JSONObject generateMCQs(int amount, int grade, String subject, String details) {
        String userPrompt = getUserPrompt(amount, grade, subject, details);
        String systemPrompt = getSystemPrompt();
        JSONObject actualGPTResponse = getGPTResponse(systemPrompt, userPrompt);

        if (!actualGPTResponse.has("MCQs")) {
            System.out.println("SUYA");
            JSONObject errorResponse = new JSONObject();
            errorResponse.put("status", 400);
            errorResponse.put("error", "invalid prompt");
            return errorResponse;
        }

        boolean isResponseFormatCorrect = checkGPTResponse(actualGPTResponse.getJSONArray("MCQs"));

        if (isResponseFormatCorrect) {
            JSONObject successResponse = new JSONObject();
            successResponse.put("status", 200);
            successResponse.put("MCQs", MCQs);
            return successResponse;
        } else {
            JSONObject errorResponse = new JSONObject();
            errorResponse.put("status", 400);
            errorResponse.put("error", "bad response format");
            return errorResponse;
        }
    }

    private static String getUserPrompt(int amount, int grade, String subject, String details) {
        return String.format(
                "Generate %d multiple-choice questions for %dth Grade in %s. %s. Provide the questions in the following format:\n[\n    {\n        \"Q\": \"This is the Question in sentence form.\",\n        \"O\": [\"A1\", \"A2\", \"A3\", \"A4\"],\n        \"A\": 0\n    },\n    {\n        \"Q\": \"This is the Question in sentence form.\",\n        \"O\": [\"A1\", \"A2\", \"A3\", \"A4\"],\n        \"A\": 0\n    },\n    and so on\n]",
                amount, grade, subject, details
        );
    }

    private static String getSystemPrompt() {
        return """
            -> You are a Question Bank Generator. Your job is to generate multiple-choice questions (MCQs) based on the user's specifications. Each question should have 4 options, and one correct answer.
            -> If the user asks for anything not related to generating MCQs, you will respond with "Irrelevant Prompt".
            -> Your responses should be in JSON format.
            -> Example relevant prompts include: 
                -> 'Generate 10 MCQs for 12th grade Physics on Harmonic Oscillations.'
                -> 'Create 5 multiple-choice questions for university-level biology on cellular respiration.'
            -> Example irrelevant prompts include:
                -> 'What is the weather today?'
                -> 'Who is the president of the United States?'
            """;
    }

    private static JSONObject getGPTResponse(String systemPrompt, String userInput) {
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpPost post = new HttpPost("https://api.openai.com/v1/chat/completions");
            post.setHeader("Content-type", "application/json");
            post.setHeader("Authorization", "Bearer " + System.getenv("OPENAI_API_KEY"));

            JSONObject body = new JSONObject();
            body.put("model", "gpt-3.5-turbo");
            JSONArray messages = new JSONArray();
            messages.put(new JSONObject().put("role", "system").put("content", systemPrompt));
            messages.put(new JSONObject().put("role", "user").put("content", userInput));
            body.put("messages", messages);

            post.setEntity(new StringEntity(body.toString()));

            String response = EntityUtils.toString(client.execute(post).getEntity());
            System.out.println("GPT Response: " + response); // Log the response

            JSONObject jsonResponse = new JSONObject(response);
            JSONArray choices = jsonResponse.getJSONArray("choices");
            if (choices.length() > 0) {
                String content = choices.getJSONObject(0).getJSONObject("message").getString("content");
                // Parse the content to extract the MCQs array
                JSONObject contentJson = new JSONObject(content);
                JSONArray mcqsArray;
                try {
                     mcqsArray = contentJson.getJSONArray("questions");
                }
                catch(Exception e) {
                     mcqsArray = contentJson.getJSONArray("Questions");
                }

                return new JSONObject().put("MCQs", mcqsArray);
            } else {
                throw new RuntimeException("No choices in GPT response");
            }
        } catch (IOException e) {
            e.printStackTrace(); // Print stack trace to help with debugging
            throw new RuntimeException("Error in GPT call: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace(); // Catch any other exceptions
            throw new RuntimeException("Unexpected error: " + e.getMessage());
        }
    }

    private static boolean checkGPTResponse(JSONArray responseMCQs) {
        MCQs = responseMCQs;
        for (int i = 0; i < MCQs.length(); i++) {
            JSONObject mcq = MCQs.getJSONObject(i);
            if (!mcq.has("Q") || !mcq.has("O") || !mcq.has("A")) return false;
            JSONArray options = mcq.getJSONArray("O");
            if (options.length() != 4) return false;
            int answerIndex = mcq.getInt("A");
            if (answerIndex < 0 || answerIndex >= options.length()) return false;
        }

        return true;
    }
}