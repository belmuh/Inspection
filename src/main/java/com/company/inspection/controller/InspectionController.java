package com.company.inspection.controller;

import com.company.inspection.dto.request.CreateInspectionRequest;
import com.company.inspection.dto.response.InspectionResponse;
import com.company.inspection.service.InspectionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/inspections")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*") // Allow mobile app access
public class InspectionController {

    private final InspectionService inspectionService;

    /**
     * READ METHOD: Get inspection questions with previous data for a car
     *
     * GET /api/v1/inspections/{carId}/questions
     *
     * Test case requirement: This method takes "carId" as parameter and returns
     * an array of objects containing questions and details. If previous inspection
     * exists for this carId and "Yes" option was selected, photo URLs from
     * previous inspection are returned as sub-information in the objects.
     */
    @GetMapping("/{carId}/questions")
    public ResponseEntity<InspectionResponse> getInspectionQuestions(
            @PathVariable("carId") String carId) {

        log.info("GET /api/v1/inspections/{}/questions - Getting inspection questions", carId);

        try {
            InspectionResponse response = inspectionService.getInspectionQuestions(carId);

            log.info("Successfully retrieved {} questions for car: {}, has previous inspection: {}",
                    response.getQuestions().size(), carId, response.getHasPreviousInspection());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error getting inspection questions for car: {}", carId, e);
            throw e;
        }
    }

    /**
     * CREATE METHOD: Create new inspection with answers
     *
     * POST /api/v1/inspections
     *
     * Test case requirement: This method is called after questions are answered
     * in the inspection screen created using the Read method, by adding "carId"
     * information. Photos taken are automatically uploaded to cloud by mobile app
     * and sent to this service as an array of strings containing user's selected
     * answer and photo URL information.
     */
    @PostMapping
    public ResponseEntity<InspectionResponse> createInspection(
            @Valid @RequestBody CreateInspectionRequest request) {

        log.info("POST /api/v1/inspections - Creating inspection for car: {} with {} answers",
                request.getCarId(), request.getAnswers().size());

        try {
            InspectionResponse response = inspectionService.createInspection(request);

            log.info("Successfully created inspection with id: {} for car: {}",
                    response.getInspectionId(), request.getCarId());

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (IllegalArgumentException e) {
            log.warn("Invalid request for creating inspection: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error creating inspection for car: {}", request.getCarId(), e);
            throw e;
        }
    }

    /**
     * Get specific inspection by ID (optional endpoint for debugging/admin)
     */
    @GetMapping("/{inspectionId}")
    public ResponseEntity<Object> getInspectionById(@PathVariable("inspectionId") Long inspectionId) {

        log.info("GET /api/v1/inspections/{} - Getting inspection details", inspectionId);

        try {
            var inspection = inspectionService.getInspectionById(inspectionId);

            // Simple response for debugging
            var response = java.util.Map.of(
                    "inspectionId", inspection.getId(),
                    "carId", inspection.getCarId(),
                    "status", inspection.getStatus(),
                    "inspectionDate", inspection.getInspectionDate(),
                    "createdAt", inspection.getCreatedAt(),
                    "answerCount", inspection.getAnswers().size()
            );

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error getting inspection: {}", inspectionId, e);
            throw e;
        }
    }

    /**
     * Get all inspections for a car (optional endpoint for history)
     */
    @GetMapping("/car/{carId}")
    public ResponseEntity<Object> getInspectionsByCarId(@PathVariable("carId") String carId) {

        log.info("GET /api/v1/inspections/car/{} - Getting inspection history", carId);

        try {
            var inspections = inspectionService.getInspectionsByCarId(carId);

            // Simple response with basic info
            var response = inspections.stream()
                    .map(inspection -> java.util.Map.of(
                            "inspectionId", inspection.getId(),
                            "status", inspection.getStatus(),
                            "inspectionDate", inspection.getInspectionDate(),
                            "createdAt", inspection.getCreatedAt()
                    ))
                    .collect(java.util.stream.Collectors.toList());

            var result = java.util.Map.of(
                    "carId", carId,
                    "totalInspections", inspections.size(),
                    "inspections", response
            );

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            log.error("Error getting inspections for car: {}", carId, e);
            throw e;
        }
    }

    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<Object> healthCheck() {
        var response = java.util.Map.of(
                "status", "UP",
                "service", "Inspection Service",
                "timestamp", java.time.LocalDateTime.now()
        );
        return ResponseEntity.ok(response);
    }
}