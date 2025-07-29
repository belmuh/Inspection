package com.company.inspection.repository;

import com.company.inspection.entity.InspectionAnswer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InspectionAnswerRepository extends JpaRepository<InspectionAnswer, Long> {

    // Find answers by inspection ID
    List<InspectionAnswer> findByInspectionId(Long inspectionId);

    // Find answers by inspection ID with photos
    @Query("SELECT a FROM InspectionAnswer a " +
            "LEFT JOIN FETCH a.photos " +
            "WHERE a.inspection.id = :inspectionId")
    List<InspectionAnswer> findByInspectionIdWithPhotos(@Param("inspectionId") Long inspectionId);

    // Find answers by question ID
    List<InspectionAnswer> findByQuestionId(Long questionId);

    // Find specific answer by inspection and question
    Optional<InspectionAnswer> findByInspectionIdAndQuestionId(Long inspectionId, Long questionId);

    // Find YES answers for an inspection (answers that have photos)
    List<InspectionAnswer> findByInspectionIdAndAnswer(Long inspectionId, InspectionAnswer.AnswerType answer);

    // Find YES answers with photos
    @Query("SELECT a FROM InspectionAnswer a " +
            "LEFT JOIN FETCH a.photos " +
            "WHERE a.inspection.id = :inspectionId AND a.answer = 'YES'")
    List<InspectionAnswer> findYesAnswersWithPhotos(@Param("inspectionId") Long inspectionId);

    // Find latest answers for a car by question
    @Query("SELECT a FROM InspectionAnswer a " +
            "JOIN a.inspection i " +
            "WHERE i.carId = :carId AND a.question.id = :questionId " +
            "ORDER BY i.createdAt DESC")
    List<InspectionAnswer> findLatestAnswersByCarAndQuestion(@Param("carId") String carId, @Param("questionId") Long questionId);

    // Find latest answer for a car by question (single result)
    @Query("SELECT a FROM InspectionAnswer a " +
            "JOIN a.inspection i " +
            "WHERE i.carId = :carId AND a.question.id = :questionId " +
            "ORDER BY i.createdAt DESC")
    Optional<InspectionAnswer> findLatestAnswerByCarAndQuestion(@Param("carId") String carId, @Param("questionId") Long questionId);

    // Count answers by type for an inspection
    long countByInspectionIdAndAnswer(Long inspectionId, InspectionAnswer.AnswerType answer);

    // Find answers with photos for a specific car's latest inspection
    @Query("SELECT a FROM InspectionAnswer a " +
            "LEFT JOIN FETCH a.photos p " +
            "JOIN a.inspection i " +
            "WHERE i.carId = :carId AND i.id = (" +
            "    SELECT MAX(i2.id) FROM Inspection i2 WHERE i2.carId = :carId AND i2.status = 'COMPLETED'" +
            ") AND a.answer = 'YES'")
    List<InspectionAnswer> findLatestYesAnswersWithPhotos(@Param("carId") String carId);

    // Get answer statistics for an inspection
    @Query("SELECT a.answer, COUNT(a) FROM InspectionAnswer a " +
            "WHERE a.inspection.id = :inspectionId " +
            "GROUP BY a.answer")
    List<Object[]> getAnswerStatsByInspection(@Param("inspectionId") Long inspectionId);

    // Check if all questions are answered for an inspection
    @Query("SELECT COUNT(DISTINCT a.question.id) FROM InspectionAnswer a WHERE a.inspection.id = :inspectionId")
    long countAnsweredQuestions(@Param("inspectionId") Long inspectionId);
}