package com.company.inspection.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateInspectionRequest {

    @NotBlank(message = "Car ID cannot be blank")
    @Size(max = 100, message = "Car ID cannot exceed 100 characters")
    private String carId;

    @NotEmpty(message = "Answers cannot be empty")
    @Valid
    private List<AnswerRequest> answers;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AnswerRequest {

        @NotNull(message = "Question ID cannot be null")
        private Long questionId;

        @NotBlank(message = "Answer cannot be blank")
        private String answer; // "YES" or "NO"

        @Size(max = 1000, message = "Description cannot exceed 1000 characters")
        private String description; // Required for YES answers

        @Size(max = 3, message = "Maximum 3 photos allowed")
        private List<@NotBlank(message = "Photo URL cannot be blank") String> photoUrls; // Required for YES answers
    }
}