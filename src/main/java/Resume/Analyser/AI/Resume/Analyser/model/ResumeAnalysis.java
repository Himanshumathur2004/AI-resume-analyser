package Resume.Analyser.AI.Resume.Analyser.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
public class ResumeAnalysis {

    public ResumeAnalysis() {}

    public ResumeAnalysis(Long id, String fileName, String originalFileName, String fileType, Long fileSize, 
                         String candidateName, String candidateEmail, Integer overallScore, Integer atsScore, 
                         Integer skillsScore, Integer experienceScore, Integer educationScore, Integer formattingScore, 
                         String extractedSkills, String workExperience, String educationDetails, String strengths, 
                         String weaknesses, String suggestions, String keywordsFound, String keywordsMissing, 
                         String summary, String interviewQuestions, Integer jdMatchScore, String rawText, 
                         String analysisStatus, String errorMessage, User user, LocalDateTime createdAt, 
                         LocalDateTime updatedAt) {
        this.id = id;
        this.fileName = fileName;
        this.originalFileName = originalFileName;
        this.fileType = fileType;
        this.fileSize = fileSize;
        this.candidateName = candidateName;
        this.candidateEmail = candidateEmail;
        this.overallScore = overallScore;
        this.atsScore = atsScore;
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
        this.jdMatchScore = jdMatchScore;
        this.rawText = rawText;
        this.analysisStatus = analysisStatus;
        this.errorMessage = errorMessage;
        this.user = user;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "file_name", nullable = false)
    private String fileName;

    @Column(name = "original_file_name")
    private String originalFileName;

    @Column(name = "file_type")
    private String fileType;

    @Column(name = "file_size")
    private Long fileSize;

    @Column(name = "candidate_name")
    private String candidateName;

    @Column(name = "candidate_email")
    private String candidateEmail;

    @Column(name = "overall_score")
    private Integer overallScore;

    @Column(name = "ats_score")
    private Integer atsScore;

    @Column(name = "skills_score")
    private Integer skillsScore;

    @Column(name = "experience_score")
    private Integer experienceScore;

    @Column(name = "education_score")
    private Integer educationScore;

    @Column(name = "formatting_score")
    private Integer formattingScore;

    @Column(name = "extracted_skills", columnDefinition = "TEXT")
    private String extractedSkills;

    @Column(name = "work_experience", columnDefinition = "TEXT")
    private String workExperience;

    @Column(name = "education_details", columnDefinition = "TEXT")
    private String educationDetails;

    @Column(name = "strengths", columnDefinition = "TEXT")
    private String strengths;

    @Column(name = "weaknesses", columnDefinition = "TEXT")
    private String weaknesses;

    @Column(name = "suggestions", columnDefinition = "TEXT")
    private String suggestions;

    @Column(name = "keywords_found", columnDefinition = "TEXT")
    private String keywordsFound;

    @Column(name = "keywords_missing", columnDefinition = "TEXT")
    private String keywordsMissing;

    @Column(name = "summary", columnDefinition = "TEXT")
    private String summary;

    @Column(name = "interview_questions", columnDefinition = "TEXT")
    private String interviewQuestions;

    @Column(name = "jd_match_score")
    private Integer jdMatchScore;

    @Column(name = "raw_text", columnDefinition = "LONGTEXT")
    private String rawText;

    @Column(name = "analysis_status")
    private String analysisStatus; // PENDING, PROCESSING, COMPLETED, FAILED

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @ManyToOne(fetch = jakarta.persistence.FetchType.LAZY)
    @jakarta.persistence.JoinColumn(name = "user_id")
    private User user;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        analysisStatus = "PENDING";
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

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
    public Integer getSkillsScore() { return skillsScore; }
    public void setSkillsScore(Integer skillsScore) { this.skillsScore = skillsScore; }
    public Integer getExperienceScore() { return experienceScore; }
    public void setExperienceScore(Integer experienceScore) { this.experienceScore = experienceScore; }
    public Integer getEducationScore() { return educationScore; }
    public void setEducationScore(Integer educationScore) { this.educationScore = educationScore; }
    public Integer getFormattingScore() { return formattingScore; }
    public void setFormattingScore(Integer formattingScore) { this.formattingScore = formattingScore; }
    public String getExtractedSkills() { return extractedSkills; }
    public void setExtractedSkills(String extractedSkills) { this.extractedSkills = extractedSkills; }
    public String getWorkExperience() { return workExperience; }
    public void setWorkExperience(String workExperience) { this.workExperience = workExperience; }
    public String getEducationDetails() { return educationDetails; }
    public void setEducationDetails(String educationDetails) { this.educationDetails = educationDetails; }
    public String getStrengths() { return strengths; }
    public void setStrengths(String strengths) { this.strengths = strengths; }
    public String getWeaknesses() { return weaknesses; }
    public void setWeaknesses(String weaknesses) { this.weaknesses = weaknesses; }
    public String getSuggestions() { return suggestions; }
    public void setSuggestions(String suggestions) { this.suggestions = suggestions; }
    public String getKeywordsFound() { return keywordsFound; }
    public void setKeywordsFound(String keywordsFound) { this.keywordsFound = keywordsFound; }
    public String getKeywordsMissing() { return keywordsMissing; }
    public void setKeywordsMissing(String keywordsMissing) { this.keywordsMissing = keywordsMissing; }
    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }
    public String getInterviewQuestions() { return interviewQuestions; }
    public void setInterviewQuestions(String interviewQuestions) { this.interviewQuestions = interviewQuestions; }
    public Integer getJdMatchScore() { return jdMatchScore; }
    public void setJdMatchScore(Integer jdMatchScore) { this.jdMatchScore = jdMatchScore; }
    public String getRawText() { return rawText; }
    public void setRawText(String rawText) { this.rawText = rawText; }
    public String getAnalysisStatus() { return analysisStatus; }
    public void setAnalysisStatus(String analysisStatus) { this.analysisStatus = analysisStatus; }
    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
