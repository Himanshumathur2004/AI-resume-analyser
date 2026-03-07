package Resume.Analyser.AI.Resume.Analyser.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import Resume.Analyser.AI.Resume.Analyser.model.ResumeAnalysis;

@Repository
public interface ResumeAnalysisRepository extends JpaRepository<ResumeAnalysis, Long> {

    List<ResumeAnalysis> findAllByOrderByCreatedAtDesc();
    
    List<ResumeAnalysis> findByUserIdOrderByCreatedAtDesc(Long userId);

    List<ResumeAnalysis> findByAnalysisStatus(String status);

    @Query("SELECT r FROM ResumeAnalysis r WHERE r.overallScore >= :minScore ORDER BY r.overallScore DESC")
    List<ResumeAnalysis> findByScoreGreaterThanEqual(@org.springframework.data.repository.query.Param("minScore") int minScore);

    @Query("SELECT AVG(r.overallScore) FROM ResumeAnalysis r WHERE r.analysisStatus = 'COMPLETED'")
    Double findAverageScore();

    @Query("SELECT COUNT(r) FROM ResumeAnalysis r WHERE r.analysisStatus = 'COMPLETED'")
    Long countCompleted();

    Long countByUserId(Long userId);

    Long countByUserIdAndAnalysisStatus(Long userId, String status);

    @Query("SELECT AVG(r.overallScore) FROM ResumeAnalysis r WHERE r.user.id = :userId AND r.analysisStatus = 'COMPLETED'")
    Double findAverageScoreByUserId(@org.springframework.data.repository.query.Param("userId") Long userId);
}
