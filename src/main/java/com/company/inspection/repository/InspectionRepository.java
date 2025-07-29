package com.company.inspection.repository;

import com.company.inspection.entity.Inspection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface InspectionRepository extends JpaRepository<Inspection, Long> {

    // Find all inspections for a specific car
    List<Inspection> findByCarIdOrderByCreatedAtDesc(String carId);

    // Find the latest inspection for a specific car
    Optional<Inspection> findFirstByCarIdOrderByCreatedAtDesc(String carId);

    // Find completed inspections for a specific car
    List<Inspection> findByCarIdAndStatusOrderByCreatedAtDesc(String carId, Inspection.InspectionStatus status);

    // Find the latest completed inspection for a specific car
    Optional<Inspection> findFirstByCarIdAndStatusOrderByCreatedAtDesc(String carId, Inspection.InspectionStatus status);

    // Check if car has any previous inspections
    boolean existsByCarId(String carId);

    // Check if car has any completed inspections
    boolean existsByCarIdAndStatus(String carId, Inspection.InspectionStatus status);

    // Find inspections within date range
    List<Inspection> findByInspectionDateBetweenOrderByInspectionDateDesc(LocalDateTime startDate, LocalDateTime endDate);

    // Find inspections by status
    List<Inspection> findByStatusOrderByCreatedAtDesc(Inspection.InspectionStatus status);

    // Count inspections by car
    long countByCarId(String carId);

    // Count completed inspections by car
    long countByCarIdAndStatus(String carId, Inspection.InspectionStatus status);

    // Custom query to find inspections with answers and photos
    @Query("SELECT DISTINCT i FROM Inspection i " +
            "LEFT JOIN FETCH i.answers a " +
            "LEFT JOIN FETCH a.photos " +
            "WHERE i.carId = :carId " +
            "ORDER BY i.createdAt DESC")
    List<Inspection> findInspectionsWithAnswersAndPhotos(@Param("carId") String carId);

    // Find latest inspection with all related data
    @Query("SELECT i FROM Inspection i " +
            "LEFT JOIN FETCH i.answers a " +
            "LEFT JOIN FETCH a.question " +
            "LEFT JOIN FETCH a.photos " +
            "WHERE i.carId = :carId " +
            "ORDER BY i.createdAt DESC")
    Optional<Inspection> findLatestInspectionWithFullData(@Param("carId") String carId);

    // Get inspection statistics
    @Query("SELECT COUNT(i) as totalInspections, " +
            "COUNT(CASE WHEN i.status = 'COMPLETED' THEN 1 END) as completedInspections, " +
            "COUNT(CASE WHEN i.status = 'IN_PROGRESS' THEN 1 END) as inProgressInspections " +
            "FROM Inspection i WHERE i.carId = :carId")
    Object[] getInspectionStatsByCarId(@Param("carId") String carId);
}