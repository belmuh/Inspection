package com.company.inspection.repository;

import com.company.inspection.entity.Inspection;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface InspectionRepository extends JpaRepository<Inspection, Long> {
    List<Inspection> findByCarIdOrderByCreatedAtDesc(String carId);

    Optional<Inspection> findFirstByCarIdOrderByCreatedAtDesc(String carId);

    List<Inspection> findByCarIdAndCompletedOrderByCreatedAtDesc(String carId, boolean completed);

    @EntityGraph(attributePaths = {"answers", "answers.question", "answers.photos"})
    Optional<Inspection> findFirstByCarIdAndCompletedOrderByCreatedAtDesc(String carId, boolean completed);

    boolean existsByCarId(String carId);

    boolean existsByCarIdAndCompleted(String carId, boolean completed);

    List<Inspection> findByInspectionDateBetweenOrderByInspectionDateDesc(LocalDateTime startDate, LocalDateTime endDate);

    List<Inspection> findByCompletedOrderByCreatedAtDesc(boolean completed);

    long countByCarId(String carId);

    long countByCarIdAndCompleted(String carId, boolean completed);
}