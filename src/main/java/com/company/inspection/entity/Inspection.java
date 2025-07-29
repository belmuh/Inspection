package com.company.inspection.entity;


import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "inspections")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(exclude = {"answers"})
@Builder
public class Inspection {

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    @Column(name = "id")
    @EqualsAndHashCode.Include
    private Long id;

    @NotBlank(message = "Car ID cannot be blank")
    @Size(max = 100, message = "Car ID cannot exceed 100 characters")
    @Column(name = "car_id", nullable = false, length = 100)
    private String carId;

    @Column(name = "inspection_date", nullable = false)
    @Builder.Default
    private LocalDateTime inspectionDate = LocalDateTime.now();

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private InspectionStatus status = InspectionStatus.IN_PROGRESS;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    // One-to-Many relationship with InspectionAnswer
    @OneToMany(mappedBy = "inspection", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<InspectionAnswer> answers = new ArrayList<>();

    // Enum for inspection status
    public enum InspectionStatus {
        IN_PROGRESS,
        COMPLETED
    }

    // Helper methods
    public void addAnswer(InspectionAnswer answer) {
        answers.add(answer);
        answer.setInspection(this);
    }

    public void removeAnswer(InspectionAnswer answer) {
        answers.remove(answer);
        answer.setInspection(null);
    }

    public void markAsCompleted() {
        this.status = InspectionStatus.COMPLETED;
    }

    public boolean isCompleted() {
        return this.status == InspectionStatus.COMPLETED;
    }

}
