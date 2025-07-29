package com.company.inspection.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class InspectionResponse {

    // For GET /inspections/{carId}/questions response
    private String carId;
    private List<QuestionResponse> questions;
    private Boolean hasPreviousInspection;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime lastInspectionDate;

    // For POST /inspections response
    private Long inspectionId;
    private String status;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    // Success message for create operation
    private String message;

    // Static factory methods for different response types
    public static InspectionResponse forQuestions(String carId, List<QuestionResponse> questions,
                                                  Boolean hasPrevious, LocalDateTime lastInspectionDate) {
        return InspectionResponse.builder()
                .carId(carId)
                .questions(questions)
                .hasPreviousInspection(hasPrevious)
                .lastInspectionDate(lastInspectionDate)
                .build();
    }

    public static InspectionResponse forCreation(Long inspectionId, String carId,
                                                 String status, LocalDateTime createdAt) {
        return InspectionResponse.builder()
                .inspectionId(inspectionId)
                .carId(carId)
                .status(status)
                .createdAt(createdAt)
                .message("Inspection created successfully")
                .build();
    }
}