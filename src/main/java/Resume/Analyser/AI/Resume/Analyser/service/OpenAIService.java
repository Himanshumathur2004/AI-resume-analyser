package Resume.Analyser.AI.Resume.Analyser.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class OpenAIService {

    @Value("${huggingface.api.key}")
    private String apiKey;

    @Value("${huggingface.api.url}")
    private String apiUrl;

    @Value("${huggingface.model}")
    private String modelName;

    private final OkHttpClient httpClient = new OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(120, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .build();

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Analyses the resume text using HuggingFace Inference API and returns structured JSON
     */
    public String analyseResume(String resumeText, String jdText) throws IOException {
        String prompt = buildPrompt(resumeText, jdText);

        String requestBody = objectMapper.writeValueAsString(
                Map.of(
                        "model", modelName,
                        "messages", List.of(
                                Map.of("role", "user", "content", prompt)
                        ),
                        "max_tokens", 2000,
                        "temperature", 0.3
                )
        );

        Request request = new Request.Builder()
                .url(apiUrl)
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .post(RequestBody.create(requestBody, MediaType.parse("application/json")))
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            String responseBody = response.body() != null ? response.body().string() : "";

            if (!response.isSuccessful()) {
                // Give a clear, human readable error instead of raw JSON
                String hint = "";
                if (response.code() == 401 || response.code() == 403) {
                    hint = " — Check that your HuggingFace token has 'Make calls to Inference Providers' permission enabled at huggingface.co/settings/tokens";
                } else if (response.code() == 503) {
                    hint = " — Model is loading, please try again in a moment";
                }
                throw new IOException("HuggingFace API error " + response.code() + hint + ". Response: " + responseBody);
            }

            // Parse OpenAI-compatible response
            JsonNode jsonNode = objectMapper.readTree(responseBody);
            JsonNode choices = jsonNode.path("choices");

            if (!choices.isArray() || choices.size() == 0) {
                throw new IOException("Unexpected HuggingFace response: " + responseBody);
            }

            String generatedText = choices.get(0).path("message").path("content").asText();
            return extractJson(generatedText);
        }
    }

    private String buildPrompt(String resumeText, String jdText) {
        String truncatedResume = resumeText.length() > 4000
                ? resumeText.substring(0, 4000) + "\n...[truncated]" : resumeText;

        boolean hasJd = jdText != null && !jdText.trim().isEmpty();
        String truncatedJd = "";
        if (hasJd) {
            truncatedJd = jdText.length() > 2000 ? jdText.substring(0, 2000) + "\n...[truncated]" : jdText;
        }

        String prompt = "You are an expert HR professional and ATS specialist. Analyse the following resume (and Job Description if provided) and return ONLY a valid JSON object.\n\n" +
                "Return exactly this JSON structure (all fields required, scores are integers 0-100):\n" +
                "{\n" +
                "  \"candidateName\": \"Full name or Unknown\",\n" +
                "  \"candidateEmail\": \"email or empty string\",\n" +
                "  \"overallScore\": 75,\n" +
                "  \"atsScore\": 70,\n" +
                "  \"jdMatchScore\": 0,\n" +
                "  \"skillsScore\": 80,\n" +
                "  \"experienceScore\": 75,\n" +
                "  \"educationScore\": 85,\n" +
                "  \"formattingScore\": 70,\n" +
                "  \"extractedSkills\": [\"Java\", \"Spring Boot\"],\n" +
                "  \"workExperience\": \"Brief 2-3 sentence summary\",\n" +
                "  \"educationDetails\": \"Education summary\",\n" +
                "  \"strengths\": [\"strength1\", \"strength2\", \"strength3\"],\n" +
                "  \"weaknesses\": [\"weakness1\", \"weakness2\"],\n" +
                "  \"suggestions\": [\"suggestion1\", \"suggestion2\", \"suggestion3\"],\n" +
                "  \"keywordsFound\": [\"keyword1\", \"keyword2\"],\n" +
                "  \"keywordsMissing\": [\"missing1\", \"missing2\"],\n" +
                "  \"summary\": \"2-3 sentence overall assessment\",\n" +
                "  \"interviewQuestions\": [\"question1\", \"question2\", \"question3\", \"question4\", \"question5\"]\n" +
                "}\n\n" +
                "Scoring guidelines:\n" +
                "- atsScore: standard sections, no tables/images, parseable format\n" +
                "- jdMatchScore: " + (hasJd ? "Evaluate how well the candidate's skills and experience match the provided Job Description." : "If no JD is provided, just return 0.") + "\n" +
                "- skillsScore: variety, relevance, technical + soft skills\n" +
                "- experienceScore: progression, quantified results, strong action verbs\n" +
                "- educationScore: degree level, relevance, certifications\n" +
                "- formattingScore: clean layout, consistent, appropriate length\n" +
                "- overallScore: weighted avg (ATS 20%, jdMatch score 20%, Skills 20%, Experience 20%, Education 10%, Formatting 10%)\n" +
                "\n" +
                "Additional instructions:\n" +
                "- `summary`: A 2-3 sentence overview of the candidate's profile.\n" +
                "- `interviewQuestions`: Generate exactly 5 highly personalized technical and behavioral interview questions based specifically on the applicant's resume and the provided Job Description. If no JD is provided, ask questions based purely on the candidate's resume strengths and weaknesses.\n" +
                "\n" +
                "Ensure your response is valid JSON and nothing else. Do not include markdown blocks.\n";

        if (hasJd) {
            prompt += "\nCheck the extracted skills and specifically point out suggestions and missing keywords based on the Job Description.\n\n" +
                    "JOB DESCRIPTION:\n---\n" + truncatedJd + "\n---\n\n";
        }

        prompt += "RESUME:\n---\n" + truncatedResume + "\n---\n\n" +
                "Respond with ONLY the JSON object, no other text.";
        return prompt;
    }

    private String extractJson(String text) throws IOException {
        if (text == null || text.trim().isEmpty()) {
            throw new IOException("Empty response from model");
        }

        text = text.trim();
        if (text.startsWith("```")) {
            text = text.replaceAll("^```[a-zA-Z]*\\n?", "").replaceAll("```$", "").trim();
        }

        int start = text.indexOf('{');
        int end = text.lastIndexOf('}');

        if (start == -1 || end == -1 || end <= start) {
            throw new IOException("No valid JSON in response: " + text.substring(0, Math.min(200, text.length())));
        }

        String jsonCandidate = text.substring(start, end + 1);

        try {
            objectMapper.readTree(jsonCandidate);
            return jsonCandidate;
        } catch (Exception e) {
            jsonCandidate = jsonCandidate
                    .replaceAll(",\\s*}", "}")
                    .replaceAll(",\\s*]", "]");
            try {
                objectMapper.readTree(jsonCandidate);
                return jsonCandidate;
            } catch (Exception e2) {
                throw new IOException("Invalid JSON from model: " + e.getMessage());
            }
        }
    }
}
