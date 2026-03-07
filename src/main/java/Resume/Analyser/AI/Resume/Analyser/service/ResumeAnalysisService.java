package Resume.Analyser.AI.Resume.Analyser.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import Resume.Analyser.AI.Resume.Analyser.dto.ResumeAnalysisResponse;
import Resume.Analyser.AI.Resume.Analyser.model.ResumeAnalysis;
import Resume.Analyser.AI.Resume.Analyser.model.User;
import Resume.Analyser.AI.Resume.Analyser.repository.ResumeAnalysisRepository;

@Service
public class ResumeAnalysisService {

    private static final Logger log = LoggerFactory.getLogger(ResumeAnalysisService.class);
    private final FileParserService fileParserService;
    private final OpenAIService openAIService;
    private final ResumeAnalysisRepository repository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public ResumeAnalysisService(FileParserService fileParserService, OpenAIService openAIService, ResumeAnalysisRepository repository) {
        this.fileParserService = fileParserService;
        this.openAIService = openAIService;
        this.repository = repository;
    }

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    /**
     * Main method: handles file upload, text extraction, OpenAI analysis, DB save
     */
    public ResumeAnalysisResponse analyseResume(MultipartFile file, String jdText, MultipartFile jdFile, User user) throws IOException {
        // Validate file
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Please upload a non-empty file.");
        }

        long maxSize = 10 * 1024 * 1024; // 10MB
        if (file.getSize() > maxSize) {
            throw new IllegalArgumentException("File size exceeds 10MB limit.");
        }

        // Save file info to DB (status: PROCESSING)
        ResumeAnalysis analysis = new ResumeAnalysis();
        analysis.setOriginalFileName(file.getOriginalFilename());
        analysis.setFileType(fileParserService.getFileType(file.getContentType(), file.getOriginalFilename()));
        analysis.setFileSize(file.getSize());
        analysis.setAnalysisStatus("PROCESSING");

        // Read bytes UPFRONT since saving the file might consume the underlying temp stream
        byte[] fileBytes = file.getBytes();

        // Store file
        String savedFileName = saveFile(fileBytes, file.getOriginalFilename());
        analysis.setFileName(savedFileName);
        analysis.setUser(user);
        analysis = repository.save(analysis);

        try {
            // Extract text
            log.info("Extracting text from file: {}", file.getOriginalFilename());
            String extractedText = fileParserService.extractTextFromBytes(fileBytes, file.getContentType(), file.getOriginalFilename());

            if (extractedText.trim().isEmpty()) {
                throw new IllegalArgumentException("Could not extract any text from the file. Please ensure the file is not scanned/image-only.");
            }

            analysis.setRawText(extractedText.length() > 65000
                    ? extractedText.substring(0, 65000) : extractedText);

            String resolvedJdText = "";
            if (jdText != null && !jdText.trim().isEmpty()) {
                resolvedJdText = jdText.trim();
            } else if (jdFile != null && !jdFile.isEmpty()) {
                log.info("Extracting text from JD file: {}", jdFile.getOriginalFilename());
                resolvedJdText = fileParserService.extractTextFromBytes(jdFile.getBytes(), jdFile.getContentType(), jdFile.getOriginalFilename());
            }

            // Call OpenAI
            log.info("Sending resume to OpenAI for analysis...");
            String aiResponse = openAIService.analyseResume(extractedText, resolvedJdText);
            log.info("Received analysis from OpenAI");

            // Parse AI response
            JsonNode aiJson = objectMapper.readTree(aiResponse);
            mapAiResponseToEntity(aiJson, analysis);
            analysis.setAnalysisStatus("COMPLETED");

        } catch (Exception e) {
            log.error("Error during resume analysis: {}", e.getMessage(), e);
            analysis.setAnalysisStatus("FAILED");
            analysis.setErrorMessage(e.getMessage());
            analysis = repository.save(analysis);
            throw new IOException("Analysis failed: " + e.getMessage(), e);
        }

        analysis = repository.save(analysis);
        return mapToResponse(analysis);
    }

    private String saveFile(byte[] fileBytes, String originalFilename) throws IOException {
        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }
        String uniqueName = UUID.randomUUID() + "_" + originalFilename;
        Path filePath = uploadPath.resolve(uniqueName);
        Files.write(filePath, fileBytes);
        return uniqueName;
    }

    private void mapAiResponseToEntity(JsonNode json, ResumeAnalysis analysis) {
        analysis.setCandidateName(getTextOrDefault(json, "candidateName", "Unknown"));
        analysis.setCandidateEmail(getTextOrDefault(json, "candidateEmail", ""));
        analysis.setOverallScore(getIntOrDefault(json, "overallScore", 0));
        analysis.setAtsScore(getIntOrDefault(json, "atsScore", 0));
        analysis.setJdMatchScore(getIntOrDefault(json, "jdMatchScore", 0));
        analysis.setSkillsScore(getIntOrDefault(json, "skillsScore", 0));
        analysis.setExperienceScore(getIntOrDefault(json, "experienceScore", 0));
        analysis.setEducationScore(getIntOrDefault(json, "educationScore", 0));
        analysis.setFormattingScore(getIntOrDefault(json, "formattingScore", 0));
        analysis.setWorkExperience(getTextOrDefault(json, "workExperience", ""));
        analysis.setEducationDetails(getTextOrDefault(json, "educationDetails", ""));
        analysis.setSummary(getTextOrDefault(json, "summary", ""));

        // Convert arrays to JSON strings for storage
        try {
            analysis.setExtractedSkills(objectMapper.writeValueAsString(getArrayOrEmpty(json, "extractedSkills")));
            analysis.setStrengths(objectMapper.writeValueAsString(getArrayOrEmpty(json, "strengths")));
            analysis.setWeaknesses(objectMapper.writeValueAsString(getArrayOrEmpty(json, "weaknesses")));
            analysis.setSuggestions(objectMapper.writeValueAsString(getArrayOrEmpty(json, "suggestions")));
            analysis.setKeywordsFound(objectMapper.writeValueAsString(getArrayOrEmpty(json, "keywordsFound")));
            analysis.setKeywordsMissing(objectMapper.writeValueAsString(getArrayOrEmpty(json, "keywordsMissing")));
            analysis.setInterviewQuestions(objectMapper.writeValueAsString(getArrayOrEmpty(json, "interviewQuestions")));
        } catch (Exception e) {
            log.warn("Error serializing array fields: {}", e.getMessage());
        }
    }

    private String getTextOrDefault(JsonNode json, String field, String def) {
        JsonNode node = json.get(field);
        return (node != null && !node.isNull()) ? node.asText(def) : def;
    }

    private int getIntOrDefault(JsonNode json, String field, int def) {
        JsonNode node = json.get(field);
        return (node != null && !node.isNull()) ? node.asInt(def) : def;
    }

    private List<String> getArrayOrEmpty(JsonNode json, String field) {
        JsonNode node = json.get(field);
        if (node == null || !node.isArray()) return List.of();
        List<String> list = new ArrayList<>();
        node.forEach(item -> list.add(item.asText()));
        return list;
    }

    public ResumeAnalysisResponse mapToResponse(ResumeAnalysis analysis) {
        ResumeAnalysisResponse response = new ResumeAnalysisResponse();
        response.setId(analysis.getId());
        response.setFileName(analysis.getFileName());
        response.setOriginalFileName(analysis.getOriginalFileName());
        response.setFileType(analysis.getFileType());
        response.setFileSize(analysis.getFileSize());
        response.setCandidateName(analysis.getCandidateName());
        response.setCandidateEmail(analysis.getCandidateEmail());
        response.setOverallScore(analysis.getOverallScore());
        response.setAtsScore(analysis.getAtsScore());
        response.setJdMatchScore(analysis.getJdMatchScore());
        response.setSkillsScore(analysis.getSkillsScore());
        response.setExperienceScore(analysis.getExperienceScore());
        response.setEducationScore(analysis.getEducationScore());
        response.setFormattingScore(analysis.getFormattingScore());
        response.setWorkExperience(analysis.getWorkExperience());
        response.setEducationDetails(analysis.getEducationDetails());
        response.setSummary(analysis.getSummary());
        response.setAnalysisStatus(analysis.getAnalysisStatus());
        response.setErrorMessage(analysis.getErrorMessage());
        response.setCreatedAt(analysis.getCreatedAt());
        response.setUpdatedAt(analysis.getUpdatedAt());

        // Compute grade
        int score = analysis.getOverallScore() != null ? analysis.getOverallScore() : 0;
        response.setGrade(computeGrade(score));
        response.setGradeLabel(computeGradeLabel(score));

        // Deserialize arrays
        try {
            TypeReference<List<String>> listType = new TypeReference<>() {};
            if (analysis.getExtractedSkills() != null)
                response.setExtractedSkills(objectMapper.readValue(analysis.getExtractedSkills(), listType));
            if (analysis.getStrengths() != null)
                response.setStrengths(objectMapper.readValue(analysis.getStrengths(), listType));
            if (analysis.getWeaknesses() != null)
                response.setWeaknesses(objectMapper.readValue(analysis.getWeaknesses(), listType));
            if (analysis.getSuggestions() != null)
                response.setSuggestions(objectMapper.readValue(analysis.getSuggestions(), listType));
            if (analysis.getKeywordsFound() != null)
                response.setKeywordsFound(objectMapper.readValue(analysis.getKeywordsFound(), listType));
            if (analysis.getKeywordsMissing() != null)
                response.setKeywordsMissing(objectMapper.readValue(analysis.getKeywordsMissing(), listType));
            if (analysis.getInterviewQuestions() != null)
                response.setInterviewQuestions(objectMapper.readValue(analysis.getInterviewQuestions(), listType));
        } catch (Exception e) {
            log.warn("Error deserializing arrays for response: {}", e.getMessage());
        }

        return response;
    }

    private String computeGrade(int score) {
        if (score >= 90) return "A+";
        if (score >= 80) return "A";
        if (score >= 70) return "B+";
        if (score >= 60) return "B";
        if (score >= 50) return "C";
        if (score >= 40) return "D";
        return "F";
    }

    private String computeGradeLabel(int score) {
        if (score >= 80) return "Excellent";
        if (score >= 65) return "Good";
        if (score >= 50) return "Average";
        if (score >= 35) return "Needs Work";
        return "Poor";
    }

    public List<ResumeAnalysisResponse> getAllAnalyses() {
        return repository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public List<ResumeAnalysisResponse> getAnalysesByUser(Long userId) {
        return repository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(this::mapToResponse)
                .toList();
    }

    public Optional<ResumeAnalysisResponse> getAnalysisById(Long id) {
        return repository.findById(id).map(this::mapToResponse);
    }

    public void deleteAnalysis(Long id) {
        repository.deleteById(id);
    }

    public Map<String, Object> getDashboardStats(Long userId) {
        Map<String, Object> stats = new HashMap<>();
        if (userId != null) {
            stats.put("totalAnalyses", repository.countByUserId(userId));
            stats.put("completedAnalyses", repository.countByUserIdAndAnalysisStatus(userId, "COMPLETED"));
            stats.put("averageScore", repository.findAverageScoreByUserId(userId));
        } else {
            // If No user ID, return zeros instead of global stats
            stats.put("totalAnalyses", 0L);
            stats.put("completedAnalyses", 0L);
            stats.put("averageScore", null);
        }
        return stats;
    }
}
