package com.company.inspection.repository;

import com.company.inspection.entity.InspectionPhoto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface InspectionPhotoRepository extends JpaRepository<InspectionPhoto, Long> {

    // Find photos by answer ID
    List<InspectionPhoto> findByAnswerId(Long answerId);

    // Find new photos by answer ID
    List<InspectionPhoto> findByAnswerIdAndIsNew(Long answerId, Boolean isNew);

    // Find photos by inspection ID (through answer relationship)
    @Query("SELECT p FROM InspectionPhoto p " +
            "JOIN p.answer a " +
            "WHERE a.inspection.id = :inspectionId")
    List<InspectionPhoto> findByInspectionId(@Param("inspectionId") Long inspectionId);

    // Find new photos by inspection ID
    @Query("SELECT p FROM InspectionPhoto p " +
            "JOIN p.answer a " +
            "WHERE a.inspection.id = :inspectionId AND p.isNew = true")
    List<InspectionPhoto> findNewPhotosByInspectionId(@Param("inspectionId") Long inspectionId);

    // Find previous photos by inspection ID
    @Query("SELECT p FROM InspectionPhoto p " +
            "JOIN p.answer a " +
            "WHERE a.inspection.id = :inspectionId AND p.isNew = false")
    List<InspectionPhoto> findPreviousPhotosByInspectionId(@Param("inspectionId") Long inspectionId);

    // Find photos by car ID (through answer and inspection relationship)
    @Query("SELECT p FROM InspectionPhoto p " +
            "JOIN p.answer a " +
            "JOIN a.inspection i " +
            "WHERE i.carId = :carId " +
            "ORDER BY p.uploadedAt DESC")
    List<InspectionPhoto> findByCarId(@Param("carId") String carId);

    // Find latest photos for a car by question
    @Query("SELECT p FROM InspectionPhoto p " +
            "JOIN p.answer a " +
            "JOIN a.inspection i " +
            "WHERE i.carId = :carId AND a.question.id = :questionId " +
            "ORDER BY i.createdAt DESC, p.uploadedAt DESC")
    List<InspectionPhoto> findLatestPhotosByCarAndQuestion(@Param("carId") String carId, @Param("questionId") Long questionId);

    // Find photos uploaded within date range
    List<InspectionPhoto> findByUploadedAtBetweenOrderByUploadedAtDesc(LocalDateTime startDate, LocalDateTime endDate);

    // Count photos by answer
    long countByAnswerId(Long answerId);

    // Count new photos by answer
    long countByAnswerIdAndIsNew(Long answerId, Boolean isNew);

    // Count photos by inspection
    @Query("SELECT COUNT(p) FROM InspectionPhoto p " +
            "JOIN p.answer a " +
            "WHERE a.inspection.id = :inspectionId")
    long countByInspectionId(@Param("inspectionId") Long inspectionId);

    // Find photos by URL pattern (for cleanup or validation)
    @Query("SELECT p FROM InspectionPhoto p WHERE p.photoUrl LIKE %:urlPattern%")
    List<InspectionPhoto> findByPhotoUrlContaining(@Param("urlPattern") String urlPattern);

    // Find photos by file extension
    @Query("SELECT p FROM InspectionPhoto p WHERE LOWER(p.photoUrl) LIKE LOWER(CONCAT('%', :extension))")
    List<InspectionPhoto> findByFileExtension(@Param("extension") String extension);

    // Get photo statistics by inspection
    @Query("SELECT COUNT(p) as totalPhotos, " +
            "COUNT(CASE WHEN p.isNew = true THEN 1 END) as newPhotos, " +
            "COUNT(CASE WHEN p.isNew = false THEN 1 END) as previousPhotos " +
            "FROM InspectionPhoto p " +
            "JOIN p.answer a " +
            "WHERE a.inspection.id = :inspectionId")
    Object[] getPhotoStatsByInspection(@Param("inspectionId") Long inspectionId);

    // Find orphaned photos (photos without valid answers)
    @Query("SELECT p FROM InspectionPhoto p WHERE p.answer IS NULL")
    List<InspectionPhoto> findOrphanedPhotos();
}