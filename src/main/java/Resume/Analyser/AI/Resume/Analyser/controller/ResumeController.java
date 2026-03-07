package Resume.Analyser.AI.Resume.Analyser.controller;

import Resume.Analyser.AI.Resume.Analyser.dto.ApiResponse;
import Resume.Analyser.AI.Resume.Analyser.dto.ResumeAnalysisResponse;
import Resume.Analyser.AI.Resume.Analyser.model.User;
import Resume.Analyser.AI.Resume.Analyser.repository.UserRepository;
import Resume.Analyser.AI.Resume.Analyser.service.ResumeAnalysisService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/resume")
@CrossOrigin(origins = "*")
public class ResumeController {

    private static final Logger log = LoggerFactory.getLogger(ResumeController.class);
    private final ResumeAnalysisService analysisService;
    private final UserRepository userRepository;

    public ResumeController(ResumeAnalysisService analysisService, UserRepository userRepository) {
        this.analysisService = analysisService;
        this.userRepository = userRepository;
    }

    private User getAuthenticatedUser(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return null;
        }
        String token = authHeader.substring(7);
        return userRepository.findByToken(token).orElse(null);
    }

    /**
     * POST /api/resume/analyse
     * Upload and analyse a resume file
     */
    @PostMapping(value = "/analyse", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<ResumeAnalysisResponse>> analyseResume(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "jdText", required = false) String jdText,
            @RequestParam(value = "jdFile", required = false) MultipartFile jdFile) {
        
        User user = getAuthenticatedUser(authHeader);
        if (user == null) {
            return ResponseEntity.status(401).body(ApiResponse.error("Unauthorized: Please login first"));
        }

        try {
            log.info("Received resume upload: {} ({} bytes)", file.getOriginalFilename(), file.getSize());
            ResumeAnalysisResponse response = analysisService.analyseResume(file, jdText, jdFile, user);
            return ResponseEntity.ok(ApiResponse.success("Resume analysed successfully!", response));
        } catch (IllegalArgumentException e) {
            log.warn("Validation error: {}", e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("Error analysing resume: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to analyse resume: " + e.getMessage()));
        }
    }

    @GetMapping("/history")
    public ResponseEntity<ApiResponse<List<ResumeAnalysisResponse>>> getUserHistory(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        
        User user = getAuthenticatedUser(authHeader);
        if (user == null) {
            return ResponseEntity.status(401).body(ApiResponse.error("Unauthorized"));
        }
        
        List<ResumeAnalysisResponse> analyses = analysisService.getAnalysesByUser(user.getId());
        return ResponseEntity.ok(ApiResponse.success(analyses));
    }

    /**
     * GET /api/resume/all
     * Get all past resume analyses
     */
    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<ResumeAnalysisResponse>>> getAllAnalyses() {
        List<ResumeAnalysisResponse> analyses = analysisService.getAllAnalyses();
        return ResponseEntity.ok(ApiResponse.success(analyses));
    }

    /**
     * GET /api/resume/{id}
     * Get a specific analysis by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ResumeAnalysisResponse>> getAnalysisById(@PathVariable Long id) {
        return analysisService.getAnalysisById(id)
                .map(response -> ResponseEntity.ok(ApiResponse.success(response)))
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * DELETE /api/resume/{id}
     * Delete a specific analysis
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteAnalysis(@PathVariable Long id) {
        try {
            analysisService.deleteAnalysis(id);
            return ResponseEntity.ok(ApiResponse.success("Analysis deleted successfully.", null));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(ApiResponse.error("Failed to delete: " + e.getMessage()));
        }
    }

    /**
     * GET /api/resume/stats/dashboard
     * Get dashboard statistics
     */
    @GetMapping("/stats/dashboard")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getDashboardStats(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        User user = getAuthenticatedUser(authHeader);
        Map<String, Object> stats = analysisService.getDashboardStats(user != null ? user.getId() : null);
        return ResponseEntity.ok(ApiResponse.success(stats));
    }

    /**
     * GET /api/resume/health
     * Health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<ApiResponse<String>> health() {
        return ResponseEntity.ok(ApiResponse.success("AI Resume Analyser is running!"));
    }
}
