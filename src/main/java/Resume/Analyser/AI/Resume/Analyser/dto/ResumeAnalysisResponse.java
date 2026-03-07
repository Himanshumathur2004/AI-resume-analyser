package Resume.Analyser.AI.Resume.Analyser.dto;

import java.time.LocalDateTime;
import java.util.List;

public class ResumeAnalysisResponse {

    public ResumeAnalysisResponse() {}

    public ResumeAnalysisResponse(Long id, String fileName, String originalFileName, String fileType, Long fileSize, 
                                 String candidateName, String candidateEmail, Integer overallScore, Integer atsScore, 
                                 Integer jdMatchScore, Integer skillsScore, Integer experienceScore, Integer educationScore, 
                                 Integer formattingScore, List<String> extractedSkills, String workExperience, 
                                 String educationDetails, List<String> strengths, List<String> weaknesses, 
                                 List<String> suggestions, List<String> keywordsFound, List<String> keywordsMissing, 
                                 String summary, List<String> interviewQuestions, String analysisStatus, 
                                 String errorMessage, LocalDateTime createdAt, LocalDateTime updatedAt, 
                                 String grade, String gradeLabel) {
        this.id = id;
        this.fileName = fileName;
        this.originalFileName = originalFileName;
        this.fileType = fileType;
        this.fileSize = fileSize;
        this.candidateName = candidateName;
        this.candidateEmail = candidateEmail;
        this.overallScore = overallScore;
        this.atsScore = atsScore;
        this.jdMatchScore = jdMatchScore;
        this.skillsScore = skillsScore;
        this.experienceScore = experienceScore;
        this.educationScore = educationScore;
        this.formattingScore = formattingScore;
        this.extractedSkills = extractedSkills;
        this.workExperience = workExperience;
        this.educationDetails = educationDetails;
        this.strengths = strengths;
        this.weaknesses = weaknesses;
        this.suggestions = suggestions;
        this.keywordsFound = keywordsFound;
        this.keywordsMissing = keywordsMissing;
        this.summary = summary;
        this.interviewQuestions = interviewQuestions;
        this.analysisStatus = analysisStatus;
        this.errorMessage = errorMessage;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.grade = grade;
        this.gradeLabel = gradeLabel;
    }

    // Builder pattern equivalent
    public static ResumeAnalysisResponse build() {
        return new ResumeAnalysisResponse();
    }

    private Long id;
    private String fileName;
    private String originalFileName;
    private String fileType;
    private Long fileSize;

    // Candidate Info
    private String candidateName;
    private String candidateEmail;

    // Scores
    private Integer overallScore;
    private Integer atsScore;
    private Integer jdMatchScore;
    private Integer skillsScore;
    private Integer experienceScore;
    private Integer educationScore;
    private Integer formattingScore;

    // Extracted Content
    private List<String> extractedSkills;
    private String workExperience;
    private String educationDetails;

    // Analysis Results
    private List<String> strengths;
    private List<String> weaknesses;
    private List<String> suggestions;
    private List<String> keywordsFound;
    private List<String> keywordsMissing;
    private String summary;
    private List<String> interviewQuestions;

    // Status
    private String analysisStatus;
    private String errorMessage;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Score grade
    private String grade;
    private String gradeLabel;

    // Manual Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }
    public String getOriginalFileName() { return originalFileName; }
    public void setOriginalFileName(String originalFileName) { this.originalFileName = originalFileName; }
    public String getFileType() { return fileType; }
    public void setFileType(String fileType) { this.fileType = fileType; }
    public Long getFileSize() { return fileSize; }
    public void setFileSize(Long fileSize) { this.fileSize = fileSize; }
    public String getCandidateName() { return candidateName; }
    public void setCandidateName(String candidateName) { this.candidateName = candidateName; }
    public String getCandidateEmail() { return candidateEmail; }
    public void setCandidateEmail(String candidateEmail) { this.candidateEmail = candidateEmail; }
    public Integer getOverallScore() { return overallScore; }
    public void setOverallScore(Integer overallScore) { this.overallScore = overallScore; }
    public Integer getAtsScore() { return atsScore; }
    public void setAtsScore(Integer atsScore) { this.atsScore = atsScore; }
    public Integer getJdMatchScore() { return jdMatchScore; }
    public void setJdMatchScore(Integer jdMatchScore) { this.jdMatchScore = jdMatchScore; }
    public Integer getSkillsScore() { return skillsScore; }
    public void setSkillsScore(Integer skillsScore) { this.skillsScore = skillsScore; }
    public Integer getExperienceScore() { return experienceScore; }
    public void setExperienceScore(Integer experienceScore) { this.experienceScore = experienceScore; }
    public Integer getEducationScore() { return educationScore; }
    public void setEducationScore(Integer educationScore) { this.educationScore = educationScore; }
    public Integer getFormattingScore() { return formattingScore; }
    public void setFormattingScore(Integer formattingScore) { this.formattingScore = formattingScore; }
    public List<String> getExtractedSkills() { return extractedSkills; }
    public void setExtractedSkills(List<String> extractedSkills) { this.extractedSkills = extractedSkills; }
    public String getWorkExperience() { return workExperience; }
    public void setWorkExperience(String workExperience) { this.workExperience = workExperience; }
    public String getEducationDetails() { return educationDetails; }
    public void setEducationDetails(String educationDetails) { this.educationDetails = educationDetails; }
    public List<String> getStrengths() { return strengths; }
    public void setStrengths(List<String> strengths) { this.strengths = strengths; }
    public List<String> getWeaknesses() { return weaknesses; }
    public void setWeaknesses(List<String> weaknesses) { this.weaknesses = weaknesses; }
    public List<String> getSuggestions() { return suggestions; }
    public void setSuggestions(List<String> suggestions) { this.suggestions = suggestions; }
    public List<String> getKeywordsFound() { return keywordsFound; }
    public void setKeywordsFound(List<String> keywordsFound) { this.keywordsFound = keywordsFound; }
    public List<String> getKeywordsMissing() { return keywordsMissing; }
    public void setKeywordsMissing(List<String> keywordsMissing) { this.keywordsMissing = keywordsMissing; }
    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }
    public List<String> getInterviewQuestions() { return interviewQuestions; }
    public void setInterviewQuestions(List<String> interviewQuestions) { this.interviewQuestions = interviewQuestions; }
    public String getAnalysisStatus() { return analysisStatus; }
    public void setAnalysisStatus(String analysisStatus) { this.analysisStatus = analysisStatus; }
    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public String getGrade() { return grade; }
    public void setGrade(String grade) { this.grade = grade; }
    public String getGradeLabel() { return gradeLabel; }
    public void setGradeLabel(String gradeLabel) { this.gradeLabel = gradeLabel; }
}
